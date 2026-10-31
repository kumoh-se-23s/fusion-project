package service;

import exception.AccountException;
import exception.NotFoundException;
import exception.PasswordMisMatchException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.AccountData;
import model.dto.TokenPair;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.request.account.DeleteAccountRequest;
import model.dto.response.PKResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import repository.AccountRepository;
import util.AccountUtil;
import util.JwtTokenProvider;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private static final long ACCOUNT_PK = 1L;
    private static final String LOGIN_ID = "hong";
    private static final String PASSWORD = "password1234";

    private AccountService accountService;
    private FakeAccountRepository accountRepository;
    private FakeJedis jedis;
    private AccountUtil accountUtil;
    private String salt;

    // ===================================================================== 가짜 객체
    // DB와 Redis 없이 테스트하기 위해 진짜 클래스를 상속해서 필요한 메서드만 바꿔 끼운다.

    static class FakeAccountRepository extends AccountRepository {
        private final Map<Long, LocalAccount> accounts = new HashMap<>();
        private final List<Long> deletedPks = new ArrayList<>();
        private Long updatedPk;
        private String updatedPassword;
        private String updatedSalt;

        void save(LocalAccount account) {
            accounts.put(account.getPk(), account);
        }

        @Override
        public Optional<LocalAccount> findId(String id) {
            return accounts.values().stream()
                    .filter(account -> account.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<LocalAccount> findByPK(Long pk) {
            return Optional.ofNullable(accounts.get(pk));
        }

        @Override
        public boolean updatePassword(long accountPk, String hashedPassword, String salt) {
            updatedPk = accountPk;
            updatedPassword = hashedPassword;
            updatedSalt = salt;
            return true;
        }

        @Override
        public boolean delete(Long pk) {
            deletedPks.add(pk);
            return accounts.remove(pk) != null;
        }
    }

    static class FakeJedis extends Jedis {
        private final Map<String, String> store = new HashMap<>();
        private final Map<String, Long> ttls = new HashMap<>();

        @Override
        public String setex(String key, long seconds, String value) {
            store.put(key, value);
            ttls.put(key, seconds);
            return "OK";
        }

        @Override
        public long del(String key) {
            ttls.remove(key);
            return store.remove(key) == null ? 0 : 1;
        }

        @Override
        public void close() {
            // 실제 연결이 없으므로 아무것도 하지 않는다.
        }
    }

    static class FakeJedisPool extends JedisPool {
        private final FakeJedis jedis;

        FakeJedisPool(FakeJedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public Jedis getResource() {
            return jedis;
        }
    }

    // ===================================================================== 준비

    @BeforeEach
    void setUp() throws Exception {
        accountUtil = new AccountUtil();
        accountRepository = new FakeAccountRepository();
        jedis = new FakeJedis();

        salt = accountUtil.generateSalt();
        accountRepository.save(LocalAccount.builder()
                .pk(ACCOUNT_PK)
                .user(AppUser.builder().pk(10L).name("홍길동").email("hong@example.com").build())
                .id(LOGIN_ID)
                .password(accountUtil.hash(PASSWORD, salt))
                .salt(salt)
                .build());

        accountService = new AccountService(
                new JwtTokenProvider(), accountUtil, accountRepository, new FakeJedisPool(jedis));
    }

    private String subjectOf(String token) {
        // JwtTokenProvider와 같은 비밀 키로 토큰을 열어 subject를 꺼낸다.
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        "YEm5Guw205YIM4QuSKDrQqv/nmvssJwbQQc8OPCJ4lM=".getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ===================================================================== 테스트

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("아이디와 비밀번호가 맞으면 계정 pk가 담긴 토큰 두 개를 돌려준다")
        void success() {
            TokenPair tokenPair = accountService.login(new AccountData(LOGIN_ID, PASSWORD));

            assertEquals(String.valueOf(ACCOUNT_PK), subjectOf(tokenPair.getAccessToken()));
            assertEquals(String.valueOf(ACCOUNT_PK), subjectOf(tokenPair.getRefreshToken()));
        }

        @Test
        @DisplayName("로그인하면 refresh token을 Redis에 1시간(3600초) 동안 저장한다")
        void savesRefreshToken() {
            TokenPair tokenPair = accountService.login(new AccountData(LOGIN_ID, PASSWORD));

            assertEquals(tokenPair.getRefreshToken(), jedis.store.get("refreshToken" + ACCOUNT_PK));
            assertEquals(3600L, jedis.ttls.get("refreshToken" + ACCOUNT_PK));
        }

        @Test
        @DisplayName("비밀번호가 틀리면 PasswordMisMatchException이 발생하고 Redis에 저장하지 않는다")
        void wrongPassword() {
            assertThrows(PasswordMisMatchException.class,
                    () -> accountService.login(new AccountData(LOGIN_ID, "wrong")));
            assertTrue(jedis.store.isEmpty());
        }

        @Test
        @DisplayName("없는 아이디면 AccountException이 발생한다")
        void unknownId() {
            assertThrows(AccountException.class,
                    () -> accountService.login(new AccountData("nobody", PASSWORD)));
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 맞으면 새 salt로 해시한 새 비밀번호를 저장한다")
        void success() throws Exception {
            PKResponse response = accountService.changePassword(
                    ACCOUNT_PK, new ChangePasswordRequest(PASSWORD, "newPassword"));

            assertEquals(ACCOUNT_PK, response.getPk());
            assertEquals(ACCOUNT_PK, accountRepository.updatedPk);
            assertNotEquals(salt, accountRepository.updatedSalt);
            assertTrue(accountUtil.matches(
                    "newPassword", accountRepository.updatedSalt, accountRepository.updatedPassword));
        }

        @Test
        @DisplayName("현재 비밀번호가 틀리면 PasswordMisMatchException이 발생하고 저장하지 않는다")
        void wrongCurrentPassword() {
            assertThrows(PasswordMisMatchException.class,
                    () -> accountService.changePassword(
                            ACCOUNT_PK, new ChangePasswordRequest("wrong", "newPassword")));
            assertNull(accountRepository.updatedPk);
        }

        @Test
        @DisplayName("없는 계정이면 NotFoundException이 발생한다")
        void unknownAccount() {
            assertThrows(NotFoundException.class,
                    () -> accountService.changePassword(
                            999L, new ChangePasswordRequest(PASSWORD, "newPassword")));
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("Redis에 저장된 refresh token을 지운다")
        void deletesRefreshToken() {
            accountService.login(new AccountData(LOGIN_ID, PASSWORD));

            accountService.logout(ACCOUNT_PK);

            assertFalse(jedis.store.containsKey("refreshToken" + ACCOUNT_PK));
        }

        @Test
        @DisplayName("저장된 토큰이 없어도 예외 없이 끝난다")
        void noToken() {
            assertDoesNotThrow(() -> accountService.logout(ACCOUNT_PK));
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("비밀번호가 맞으면 계정을 삭제한다")
        void success() {
            accountService.deleteAccount(ACCOUNT_PK, new DeleteAccountRequest(PASSWORD));

            assertEquals(List.of(ACCOUNT_PK), accountRepository.deletedPks);
        }

        @Test
        @DisplayName("비밀번호가 틀리면 PasswordMisMatchException이 발생하고 삭제하지 않는다")
        void wrongPassword() {
            assertThrows(PasswordMisMatchException.class,
                    () -> accountService.deleteAccount(ACCOUNT_PK, new DeleteAccountRequest("wrong")));
            assertTrue(accountRepository.deletedPks.isEmpty());
        }

        @Test
        @DisplayName("없는 계정이면 NotFoundException이 발생한다")
        void unknownAccount() {
            assertThrows(NotFoundException.class,
                    () -> accountService.deleteAccount(999L, new DeleteAccountRequest(PASSWORD)));
        }
    }
}
