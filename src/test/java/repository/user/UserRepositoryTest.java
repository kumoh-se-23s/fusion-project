package repository.user;

import model.domain.AppUser;
import model.dto.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.Filter;
import repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService가 사용하는 {@link repository.UserRepository} 테스트.
 * ({@link repository.UserRepository}는 메서드가 하나도 없어서 따로 테스트하지 않는다.)
 */
class UserRepositoryTest {

    private SpyUserRepository userRepository;

    // BaseRepositoryImpl은 아직 DB와 연결되어 있지 않다.
    // 그래서 부모의 find/findById/update를 바꿔 끼워, UserRepository가 "무엇을 부탁하는지"를 기록한다.
    static class SpyUserRepository extends UserRepository {
        private List<AppUser> findResult = List.of();
        private Optional<AppUser> findByIdResult = Optional.empty();
        private Optional<AppUser> updateResult = Optional.empty();
        private Filter receivedFilter;
        private Long findByIdPk;
        private Long updatedPk;
        private Map<String, Object> updatedData;

        @Override
        public List<AppUser> find(Filter filter) {
            receivedFilter = filter;
            return findResult;
        }

        @Override
        public Optional<AppUser> findByPK(Long pk) {
            findByIdPk = pk;
            return findByIdResult;
        }

        @Override
        public Optional<AppUser> update(Long pk, Map<String, Object> updateData) {
            updatedPk = pk;
            updatedData = updateData;
            return updateResult;
        }
    }

    private static AppUser user(long pk) {
        return AppUser.builder().pk(pk).name("홍길동").email("hong@example.com").build();
    }

    @BeforeEach
    void setUp() {
        userRepository = new SpyUserRepository();
    }

    // ===== 기존 테스트 (주석 처리): findUser, findAppUserByUserPk가 UserRepository에서 삭제됨 (3e8251c) =====
//    @Nested
//    @DisplayName("findUser")
//    class FindUser {
//
//        @Test
//        @DisplayName("pk 조건으로 1개만 찾도록 Filter를 만든다")
//        void buildsFilter() {
//            userRepository.findUser(1L);
//
//            assertEquals(Map.of("pk", 1L), userRepository.receivedFilter.getConditions());
//            assertEquals(1, userRepository.receivedFilter.getLimit());
//        }
//
//        @Test
//        @DisplayName("찾은 결과가 있으면 첫 번째 사용자를 돌려준다")
//        void returnsFirst() {
//            AppUser first = user(1L);
//            userRepository.findResult = List.of(first);
//
//            assertSame(first, userRepository.findUser(1L).orElseThrow());
//        }
//
//        @Test
//        @DisplayName("찾은 결과가 없으면 빈 Optional을 돌려준다")
//        void returnsEmpty() {
//            assertTrue(userRepository.findUser(1L).isEmpty());
//        }
//    }
//
//    @Nested
//    @DisplayName("findAppUserByUserPk")
//    class FindAppUserByUserPk {
//
//        @Test
//        @DisplayName("findById에 그대로 맡기고 그 결과를 돌려준다")
//        void delegatesToFindById() {
//            AppUser found = user(1L);
//            userRepository.findByIdResult = Optional.of(found);
//
//            assertSame(found, userRepository.findAppUserByUserPk(1L).orElseThrow());
//            assertEquals(1L, userRepository.findByIdPk);
//        }
//    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("값이 있는 필드만 골라 update에 넘긴다")
        void onlyNonNullFields() {
            UserData data = UserData.builder().name("김철수").phone("010-0000-0000").build();

            userRepository.updateUser(1L, data);

            assertEquals(1L, userRepository.updatedPk);
            assertEquals(Map.of("name", "김철수", "phone", "010-0000-0000"), userRepository.updatedData);
        }

        @Test
        @DisplayName("모든 필드가 있으면 AppUser 필드 이름으로 다섯 개 키가 모두 들어간다")
        void allFields() {
            UserData data = new UserData("김철수", "kim@example.com", "부산시", "54321", "010-0000-0000");

            userRepository.updateUser(1L, data);

            assertEquals(Map.of(
                    "name", "김철수",
                    "email", "kim@example.com",
                    "address", "부산시",
                    "postal", "54321",
                    "phone", "010-0000-0000"
            ), userRepository.updatedData);
        }

        @Test
        @DisplayName("수정에 성공하면 수정된 사용자의 pk를 돌려준다")
        void returnsPk() {
            userRepository.updateResult = Optional.of(user(1L));

            assertEquals(Optional.of(1L), userRepository.updateUser(1L, UserData.builder().name("김철수").build()));
        }

        @Test
        @DisplayName("수정에 실패하면 빈 Optional을 돌려준다")
        void returnsEmpty() {
            assertTrue(userRepository.updateUser(1L, UserData.builder().name("김철수").build()).isEmpty());
        }
    }
}
