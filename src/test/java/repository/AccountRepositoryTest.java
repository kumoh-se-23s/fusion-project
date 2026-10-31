package repository;

import model.domain.AppUser;
import model.domain.LocalAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    private SpyAccountRepository accountRepository;

    // BaseRepositoryImpl은 아직 DB와 연결되어 있지 않다.
    // 그래서 부모의 create/find/update를 바꿔 끼워, AccountRepository가 "무엇을 부탁하는지"를 기록한다.
    static class SpyAccountRepository extends AccountRepository {
        private List<LocalAccount> findResult = List.of();
        private Optional<LocalAccount> updateResult = Optional.empty();
        private Filter receivedFilter;
        private Long updatedPk;
        private Map<String, Object> updatedData;
        private LocalAccount createdEntity;

        @Override
        public LocalAccount create(LocalAccount entity) {
            createdEntity = entity;
            return entity;
        }

        @Override
        public List<LocalAccount> find(Filter filter) {
            receivedFilter = filter;
            return findResult;
        }

        @Override
        public Optional<LocalAccount> update(Long pk, Map<String, Object> updateData) {
            updatedPk = pk;
            updatedData = updateData;
            return updateResult;
        }
    }

    private static LocalAccount account(long pk, String id) {
        return LocalAccount.builder()
                .pk(pk)
                .user(AppUser.builder().pk(10L).name("홍길동").email("hong@example.com").build())
                .id(id)
                .password("hashed")
                .salt("salt")
                .build();
    }

    @BeforeEach
    void setUp() {
        accountRepository = new SpyAccountRepository();
    }

    @Nested
    @DisplayName("findId")
    class FindId {

        @Test
        @DisplayName("id 조건으로 1개만 찾도록 Filter를 만든다")
        void buildsFilter() {
            accountRepository.findId("hong");

            assertEquals(Map.of("id", "hong"), accountRepository.receivedFilter.getConditions());
            assertEquals(1, accountRepository.receivedFilter.getLimit());
        }

        @Test
        @DisplayName("찾은 결과가 있으면 첫 번째 계정을 돌려준다")
        void returnsFirst() {
            LocalAccount first = account(1L, "hong");
            accountRepository.findResult = List.of(first, account(2L, "hong"));

            assertSame(first, accountRepository.findId("hong").orElseThrow());
        }

        @Test
        @DisplayName("찾은 결과가 없으면 빈 Optional을 돌려준다")
        void returnsEmpty() {
            assertTrue(accountRepository.findId("nobody").isEmpty());
        }
    }

    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("password와 salt 두 값만 담아 update를 부른다")
        void passesPasswordAndSalt() {
            accountRepository.updatePassword(1L, "newHash", "newSalt");

            assertEquals(1L, accountRepository.updatedPk);
            assertEquals(Map.of("password", "newHash", "salt", "newSalt"), accountRepository.updatedData);
        }

        @Test
        @DisplayName("update가 계정을 돌려주면 true")
        void returnsTrue() {
            accountRepository.updateResult = Optional.of(account(1L, "hong"));

            assertTrue(accountRepository.updatePassword(1L, "newHash", "newSalt"));
        }

        @Test
        @DisplayName("update가 빈 결과를 돌려주면 false")
        void returnsFalse() {
            assertFalse(accountRepository.updatePassword(1L, "newHash", "newSalt"));
        }
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        // LocalAccount의 user, salt 에는 @NonNull이 붙어 있으므로 createAccount가 이 값들도 채워야 한다.
        @Test
        @DisplayName("예외 없이 id와 password를 담은 계정을 create에 넘긴다")
        void createsAccount() {
            assertDoesNotThrow(() -> accountRepository.createAccount("hong", "password"));

            assertEquals("hong", accountRepository.createdEntity.getId());
            assertEquals("password", accountRepository.createdEntity.getPassword());
        }
    }
}
