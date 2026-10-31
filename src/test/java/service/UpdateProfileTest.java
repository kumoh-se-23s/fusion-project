package service;

import exception.NotFoundException;
import model.domain.AppUser;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.PKResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import repository.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * DB 파트 검사 항목 05. SUPPORTER - (MyPage) 개인정보 수정.
 * <p>
 * 가짜 저장소 없이 진짜 UserRepository 로 DB 에 직접 넣고, 수정하고, 다시 읽어서 확인한다.
 * 테스트용 사용자는 매 테스트마다 새로 만들고 끝나면 지운다.
 * <p>
 * DB 연결 전에는 create() 가 저장된 사용자를 돌려주지 않으므로 테스트를 건너뛴다(skipped).
 */
@DisplayName("검사 05. (MyPage) 개인정보 수정")
class UpdateProfileTest {

    private static final long UNKNOWN_PK = Long.MAX_VALUE;

    private final UserRepository userRepository = new UserRepository();
    private final UserService userService =
            new UserService(userRepository, new UserRoleRepository(), new UserRoleRelationRepository());

    private final List<Long> createdPks = new ArrayList<>();
    private AppUser supporter;
    private AppUser other;

    // 다른 데이터와 겹치지 않도록 이메일 뒤에 고유한 값을 붙인다.
    private AppUser insertUser(String name, String emailId, String postal, String address, String phone) {
        AppUser saved = userRepository.create(AppUser.builder()
                .name(name)
                .email(emailId + "+" + System.nanoTime() + "@example.com")
                .postal(postal)
                .address(address)
                .phone(phone)
                .build());

        assumeTrue(saved != null && saved.getPk() != null,
                "DB 연결 전: UserRepository.create() 가 저장된 사용자를 돌려주지 않아 건너뜁니다.");
        createdPks.add(saved.getPk());

        return userRepository.findByPK(saved.getPk()).orElseThrow();
    }

    @BeforeEach
    void setUp() {
        supporter = insertUser("홍길동", "hong", "12345", "서울시 강남구", "010-1111-1111");
        other = insertUser("김철수", "kim", "54321", "부산시 해운대구", "010-2222-2222");
    }

    @AfterEach
    void tearDown() {
        createdPks.forEach(userRepository::delete);
    }

    private PKResponse update(long userPk, UserData userData) {
        return userService.updateUser(userPk, new UpdateUserRequest(userData));
    }

    @Test
    @DisplayName("다섯 가지 개인정보를 모두 수정하면 MyPage 조회에 새 값이 보인다")
    void updateAllFields() {
        PKResponse response = update(supporter.getPk(),
                new UserData("홍길순", "soon@example.com", "서울시 마포구", "04000", "010-9999-9999"));

        UserData myInfo = userService.getMyInfo(supporter.getPk()).getUserData();

        assertEquals(supporter.getPk(), response.getPk());
        assertEquals("홍길순", myInfo.getName());
        assertEquals("soon@example.com", myInfo.getEmail());
        assertEquals("서울시 마포구", myInfo.getAddress());
        assertEquals("04000", myInfo.getPostal());
        assertEquals("010-9999-9999", myInfo.getPhone());
    }

    @Test
    @DisplayName("일부만 수정하면 나머지 개인정보는 그대로 남는다")
    void updateSomeFields() {
        update(supporter.getPk(), UserData.builder().phone("010-9999-9999").build());

        UserData myInfo = userService.getMyInfo(supporter.getPk()).getUserData();

        assertEquals("010-9999-9999", myInfo.getPhone());
        assertEquals(supporter.getName(), myInfo.getName());
        assertEquals(supporter.getEmail(), myInfo.getEmail());
        assertEquals(supporter.getAddress(), myInfo.getAddress());
        assertEquals(supporter.getPostal(), myInfo.getPostal());
    }

    @Test
    @DisplayName("개인정보가 아닌 값(pk, 가입일)은 바뀌지 않는다")
    void keepsNonProfileFields() {
        update(supporter.getPk(), new UserData("홍길순", "soon@example.com", "서울시 마포구", "04000", "010-9999-9999"));

        AppUser row = userRepository.findByPK(supporter.getPk()).orElseThrow();

        assertEquals(supporter.getPk(), row.getPk());
        assertEquals(supporter.getCreatedAt(), row.getCreatedAt());
    }

    @Test
    @DisplayName("다른 사용자의 개인정보는 바뀌지 않는다")
    void otherUserUnchanged() {
        update(supporter.getPk(), new UserData("홍길순", "soon@example.com", "서울시 마포구", "04000", "010-9999-9999"));

        UserData otherInfo = userService.getMyInfo(other.getPk()).getUserData();

        assertEquals(other.getName(), otherInfo.getName());
        assertEquals(other.getEmail(), otherInfo.getEmail());
        assertEquals(other.getAddress(), otherInfo.getAddress());
        assertEquals(other.getPostal(), otherInfo.getPostal());
        assertEquals(other.getPhone(), otherInfo.getPhone());
    }

    @Test
    @DisplayName("없는 사용자를 수정하면 NotFoundException 이 발생하고 아무것도 바뀌지 않는다")
    void unknownUser() {
        assertThrows(NotFoundException.class,
                () -> update(UNKNOWN_PK, UserData.builder().name("유령").build()));

        assertEquals(supporter.getName(), userService.getMyInfo(supporter.getPk()).getUserData().getName());
        assertEquals(other.getName(), userService.getMyInfo(other.getPk()).getUserData().getName());
    }
}
