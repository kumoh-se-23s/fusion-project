package service;

import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.PKResponse;
import model.dto.response.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.Filter;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import repository.user.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static final long USER_PK = 1L;

    private UserService userService;
    private FakeUserRepository userRepository;
    private FakeUserRoleRelationRepository userRoleRelationRepository;
    private AppUser user;

    // ===================================================================== 가짜 객체

    static class FakeUserRepository extends UserRepository {
        private final Map<Long, AppUser> users = new HashMap<>();
        private Optional<Long> updateResult;
        private Long updatedPk;
        private UserData updatedData;
        private Long deletedPk;

        // ===== 기존 코드 (주석 처리): UserRepository에서 두 메서드가 삭제됨 (3e8251c) =====
//        @Override
//        public Optional<AppUser> findUser(Long userPk) {
//            return Optional.ofNullable(users.get(userPk));
//        }
//
//        @Override
//        public Optional<AppUser> findAppUserByUserPk(long userPk) {
//            return Optional.ofNullable(users.get(userPk));
//        }

        // UserService가 이제 userRepository.findByPK(...)를 직접 부름
        @Override
        public Optional<AppUser> findByPK(Long userPk) {
            return Optional.ofNullable(users.get(userPk));
        }

        @Override
        public Optional<Long> updateUser(long userPk, UserData userData) {
            updatedPk = userPk;
            updatedData = userData;
            if (updateResult != null) return updateResult;
            // 진짜 저장소처럼 없는 사용자를 수정하면 빈 결과를 돌려준다.
            return users.containsKey(userPk) ? Optional.of(userPk) : Optional.empty();
        }

        @Override
        public boolean delete(Long pk) {
            deletedPk = pk;
            return users.remove(pk) != null;
        }
    }

    static class FakeUserRoleRelationRepository extends UserRoleRelationRepository {
        private List<UserRoleRelation> relations = List.of();
        private Filter receivedFilter;

        @Override
        public List<UserRoleRelation> find(Filter filter) {
            receivedFilter = filter;
            // 진짜 저장소처럼 limit이 있으면 그 개수까지만 돌려준다.
            if (filter.getLimit() == null) return relations;
            return relations.subList(0, Math.min(filter.getLimit(), relations.size()));
        }
    }

    // ===================================================================== 준비

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .pk(USER_PK)
                .name("홍길동")
                .email("hong@example.com")
                .postal("12345")
                .address("서울시")
                .phone("010-1234-5678")
                .build();

        userRepository = new FakeUserRepository();
        userRepository.users.put(USER_PK, user);
        userRoleRelationRepository = new FakeUserRoleRelationRepository();

        userService = new UserService(userRepository, new UserRoleRepository(), userRoleRelationRepository);
    }

    // ===================================================================== 테스트

    @Nested
    @DisplayName("getMyInfo")
    class GetMyInfo {

        @Test
        @DisplayName("사용자 정보를 UserData에 담아 돌려준다")
        void success() {
            UserResponse response = userService.getMyInfo(USER_PK);
            UserData data = response.getUserData();

            assertEquals("홍길동", data.getName());
            assertEquals("hong@example.com", data.getEmail());
            assertEquals("서울시", data.getAddress());
            assertEquals("12345", data.getPostal());
            assertEquals("010-1234-5678", data.getPhone());
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException이 발생한다")
        void unknownUser() {
            assertThrows(NotFoundException.class, () -> userService.getMyInfo(999L));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("요청의 UserData를 그대로 저장소에 넘기고, 수정된 pk를 돌려준다")
        void success() {
            UserData newData = UserData.builder().name("김철수").build();

            PKResponse response = userService.updateUser(USER_PK, new UpdateUserRequest(newData));

            assertEquals(USER_PK, response.getPk());
            assertEquals(USER_PK, userRepository.updatedPk);
            assertSame(newData, userRepository.updatedData);
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException이 발생한다")
        void unknownUser() {
            UpdateUserRequest request = new UpdateUserRequest(UserData.builder().name("김철수").build());

            assertThrows(NotFoundException.class, () -> userService.updateUser(999L, request));
        }

        @Test
        @DisplayName("저장소에서 수정에 실패하면 NotFoundException이 발생한다")
        void updateFailed() {
            userRepository.updateResult = Optional.empty();
            UpdateUserRequest request = new UpdateUserRequest(UserData.builder().name("김철수").build());

            assertThrows(NotFoundException.class, () -> userService.updateUser(USER_PK, request));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("삭제에 성공하면 result가 true다")
        void success() {
            DeleteResponse response = userService.deleteUser(USER_PK);

            assertTrue(response.getResult());
            assertEquals(USER_PK, userRepository.deletedPk);
        }

        @Test
        @DisplayName("없는 사용자면 result가 false다")
        void unknownUser() {
            DeleteResponse response = userService.deleteUser(999L);

            assertFalse(response.getResult());
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRoles {

        private UserRoleRelation relation(long pk) {
            return UserRoleRelation.builder()
                    .pk(pk)
                    .role(new UserRole())
                    .user(user)
                    .build();
        }

        @Test
        @DisplayName("저장소가 찾아 준 역할 목록을 그대로 돌려준다")
        void returnsRelations() {
            UserRoleRelation relation = relation(100L);
            userRoleRelationRepository.relations = List.of(relation);

            assertEquals(List.of(relation), userService.getRoles(USER_PK));
        }

        @Test
        @DisplayName("역할이 여러 개면 모두 돌려준다")
        void returnsAllRelations() {
            List<UserRoleRelation> relations = List.of(relation(100L), relation(101L));
            userRoleRelationRepository.relations = relations;

            assertEquals(relations, userService.getRoles(USER_PK));
        }

        // 관계 테이블 자신의 pk가 아니라, 사용자(user_fk)를 기준으로 찾아야 한다.
        @Test
        @DisplayName("찾은 사용자를 user_fk 조건으로, 개수 제한 없이 조회한다")
        void filterUsesUserWithoutLimit() {
            userService.getRoles(USER_PK);

            Filter filter = userRoleRelationRepository.receivedFilter;
            assertEquals(Map.of("user_fk", user), filter.getConditions());
            assertNull(filter.getLimit());
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException이 발생하고 역할을 조회하지 않는다")
        void unknownUser() {
            assertThrows(NotFoundException.class, () -> userService.getRoles(999L));
            assertNull(userRoleRelationRepository.receivedFilter);
        }
    }
}
