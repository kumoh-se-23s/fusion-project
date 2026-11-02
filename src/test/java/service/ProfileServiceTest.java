package service;

import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.UserRole;
import model.domain.UserRoleRelation;
import model.dto.UserData;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.Filter;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.external.ProfileService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceTest {

    private static final long USER_ID = 1L;

    private ProfileService profileService;
    private FakeUserRepository userRepository;
    private FakeUserRoleRelationRepository userRoleRelationRepository;
    private AppUser user;

    // ===================================================================== 가짜 객체

    static class FakeUserRepository extends UserRepository {
        private final Map<Long, AppUser> users = new HashMap<>();
        private Optional<Long> updateResult;
        private Long updatedId;
        private UserData updatedData;
        private Long deletedId;

        // ===== 기존 코드 (주석 처리): UserRepository에서 두 메서드가 삭제됨 (3e8251c) =====
//        @Override
//        public Optional<AppUser> findUser(Long userId) {
//            return Optional.ofNullable(users.get(userId));
//        }
//
//        @Override
//        public Optional<AppUser> findAppUserByUserId(long userId) {
//            return Optional.ofNullable(users.get(userId));
//        }

        // UserService가 이제 userRepository.findById(...)를 직접 부름
        @Override
        public Optional<AppUser> findById(Long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Optional<Long> updateUser(long userId, UserData userData) {
            updatedId = userId;
            updatedData = userData;
            if (updateResult != null) return updateResult;
            // 진짜 저장소처럼 없는 사용자를 수정하면 빈 결과를 돌려준다.
            return users.containsKey(userId) ? Optional.of(userId) : Optional.empty();
        }

        @Override
        public boolean delete(Long id) {
            deletedId = id;
            return users.remove(id) != null;
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
                .id(USER_ID)
                .name("홍길동")
                .email("hong@example.com")
                .postal("12345")
                .address("서울시")
                .phone("010-1234-5678")
                .build();

        userRepository = new FakeUserRepository();
        userRepository.users.put(USER_ID, user);
        userRoleRelationRepository = new FakeUserRoleRelationRepository();

        profileService = new ProfileService(userRepository, new UserRoleRepository(), userRoleRelationRepository);
    }

    // ===================================================================== 테스트

    @Nested
    @DisplayName("getMyInfo")
    class GetMyInfo {

        @Test
        @DisplayName("사용자 정보를 UserData에 담아 돌려준다")
        void success() {
            UserResponse response = profileService.getMyInfo(USER_ID);
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
            assertThrows(NotFoundException.class, () -> profileService.getMyInfo(999L));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("요청의 UserData를 그대로 저장소에 넘기고, 수정된 id를 돌려준다")
        void success() {
            UserData newData = UserData.builder().name("김철수").build();

            IdResponse response = profileService.updateUser(USER_ID, new UpdateUserRequest(newData));

            assertEquals(USER_ID, response.getId());
            assertEquals(USER_ID, userRepository.updatedId);
            assertSame(newData, userRepository.updatedData);
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException이 발생한다")
        void unknownUser() {
            UpdateUserRequest request = new UpdateUserRequest(UserData.builder().name("김철수").build());

            assertThrows(NotFoundException.class, () -> profileService.updateUser(999L, request));
        }

        @Test
        @DisplayName("저장소에서 수정에 실패하면 NotFoundException이 발생한다")
        void updateFailed() {
            userRepository.updateResult = Optional.empty();
            UpdateUserRequest request = new UpdateUserRequest(UserData.builder().name("김철수").build());

            assertThrows(NotFoundException.class, () -> profileService.updateUser(USER_ID, request));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("삭제에 성공하면 result가 true다")
        void success() {
            DeleteResponse response = profileService.deleteUser(USER_ID);

            assertTrue(response.getResult());
            assertEquals(USER_ID, userRepository.deletedId);
        }

        @Test
        @DisplayName("없는 사용자면 result가 false다")
        void unknownUser() {
            DeleteResponse response = profileService.deleteUser(999L);

            assertFalse(response.getResult());
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRoles {

        private UserRoleRelation relation(long id) {
            return UserRoleRelation.builder()
                    .id(id)
                    .role(new UserRole())
                    .user(user)
                    .build();
        }

        @Test
        @DisplayName("저장소가 찾아 준 역할 목록을 그대로 돌려준다")
        void returnsRelations() {
            UserRoleRelation relation = relation(100L);
            userRoleRelationRepository.relations = List.of(relation);

            assertEquals(List.of(relation), profileService.getRoles(USER_ID));
        }

        @Test
        @DisplayName("역할이 여러 개면 모두 돌려준다")
        void returnsAllRelations() {
            List<UserRoleRelation> relations = List.of(relation(100L), relation(101L));
            userRoleRelationRepository.relations = relations;

            assertEquals(relations, profileService.getRoles(USER_ID));
        }

        // 관계 테이블 자신의 id가 아니라, 사용자(user_fk)를 기준으로 찾아야 한다.
        @Test
        @DisplayName("찾은 사용자를 user_fk 조건으로, 개수 제한 없이 조회한다")
        void filterUsesUserWithoutLimit() {
            profileService.getRoles(USER_ID);

            Filter filter = userRoleRelationRepository.receivedFilter;
            assertEquals(Map.of("user_fk", user), filter.getConditions());
            assertNull(filter.getLimit());
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException이 발생하고 역할을 조회하지 않는다")
        void unknownUser() {
            assertThrows(NotFoundException.class, () -> profileService.getRoles(999L));
            assertNull(userRoleRelationRepository.receivedFilter);
        }
    }
}
