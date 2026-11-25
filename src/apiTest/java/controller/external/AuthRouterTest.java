package controller.external;

import exception.AccountException;
import exception.PasswordMisMatchException;
import exception.UnauthorizedException;
import fixture.AccountFixtures;
import model.domain.AppUser;
import model.dto.AccountData;
import model.dto.response.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.external.AuthService;
import service.external.LocalAccountService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;
import util.JwtTokenProvider;
import constant.RedisKey;
import util.RedisUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthRouter (/auth)")
class AuthRouterTest extends ApiTest {
    private static final String PASSWORD = "password1234!";

    /** 로컬 계정이 있는 유저. 로그인 아이디를 돌려준다. */
    private String createLocalUser(AppUser user) {
        String loginId = unique("login");
        AccountFixtures.createLocalAccount(user, loginId, PASSWORD);
        return loginId;
    }

    private Optional<String> accessJti(long userId) {
        return bean(RedisUtil.class).getValue(RedisKey.ACCESS, userId);
    }

    private Optional<String> refreshJti(long userId) {
        return bean(RedisUtil.class).getValue(RedisKey.REFRESH, userId);
    }

    /** 소셜 로그인 콜백이 하는 것처럼 일회용 로그인 code를 Redis에 넣는다. */
    private String putSocialLoginCode(long userId) {
        String code = UUID.randomUUID().toString();
        bean(RedisUtil.class).save(RedisKey.SOCIAL_LOGIN, code, userId, 600);
        return code;
    }

    private void assertIssuedFor(long userId, String accessToken, String refreshToken) {
        JwtTokenProvider jwt = bean(JwtTokenProvider.class);
        assertEquals(userId, jwt.getUserId(accessToken));
        assertEquals(userId, jwt.getUserId(refreshToken));
        assertEquals("access", jwt.getTokenType(accessToken));
        assertEquals("refresh", jwt.getTokenType(refreshToken));
        assertEquals(Optional.of(jwt.getJti(accessToken)), accessJti(userId), "access jti가 Redis에 없다");
        assertEquals(Optional.of(jwt.getJti(refreshToken)), refreshJti(userId), "refresh jti가 Redis에 없다");
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/auth/local-login")
    @DisplayName("POST /auth/local-login - 로컬 로그인")
    class LocalLogin {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("맞는 아이디와 비밀번호면 200과 access/refresh 토큰을 돌려준다")
            void success() {
                AppUser user = createUser();
                String loginId = createLocalUser(user);

                ApiResponse response = client.post("/auth/local-login")
                        .json(Map.of("loginId", loginId, "password", PASSWORD))
                        .send().assertStatus(200);

                String accessToken = response.json("/accessToken").asText();
                String refreshToken = response.json("/refreshToken").asText();
                assertFalse(accessToken.isBlank(), response::toString);
                assertFalse(refreshToken.isBlank(), response::toString);
                assertIssuedFor(user.getId(), accessToken, refreshToken);
            }

            @Test
            @DisplayName("받은 access 토큰으로 로그인이 필요한 API(/user/me)를 부를 수 있다")
            void issuedTokenWorks() {
                AppUser user = createUser();
                String loginId = createLocalUser(user);

                String accessToken = client.post("/auth/local-login")
                        .json(Map.of("loginId", loginId, "password", PASSWORD))
                        .send().assertStatus(200)
                        .json("/accessToken").asText();

                ApiResponse me = client.get("/user/me").bearer(accessToken).send().assertStatus(200);
                assertEquals(user.getId(), me.json("/userId").asLong());
            }

            @Test
            @DisplayName("비밀번호가 틀리면 400")
            void wrongPassword() {
                String loginId = createLocalUser(createUser());

                client.post("/auth/local-login")
                        .json(Map.of("loginId", loginId, "password", PASSWORD + "x"))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("없는 아이디면 400")
            void unknownLoginId() {
                client.post("/auth/local-login")
                        .json(Map.of("loginId", unique("nobody"), "password", PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("[BUG] 비밀번호를 빼고 보내면 400")
            void missingPassword() {
                //BUG: AuthService.localLogin이 입력 값을 검사하지 않아 null 비밀번호가 AccountUtil.hash에서 NPE -> 500
                String loginId = createLocalUser(createUser());

                client.post("/auth/local-login")
                        .json(Map.of("loginId", loginId))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                ApiResponse response = client.post("/auth/local-login").send().assertStatus(422);
                assertEquals("missing", response.json("/detail/0/type").asText(), response::toString);
            }

            @Test
            @DisplayName("깨진 JSON이면 422")
            void brokenJson() {
                client.post("/auth/local-login").rawJson("{\"loginId\": ").send().assertStatus(422);
            }

            @Test
            @DisplayName("허용하지 않은 메서드는 405")
            void wrongMethod() {
                client.get("/auth/local-login").send().assertStatus(405);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("성공하면 그 유저 id로 토큰을 만들고 jti를 Redis에 등록한다")
            void issuesTokens() {
                AppUser user = createUser();
                String loginId = createLocalUser(user);

                TokenResponse result = bean(AuthService.class).localLogin(new AccountData(loginId, PASSWORD));

                assertIssuedFor(user.getId(), result.getAccessToken(), result.getRefreshToken());
            }

            @Test
            @DisplayName("다시 로그인하면 Redis의 jti가 새 토큰 것으로 바뀐다")
            void reloginReplacesJti() {
                AppUser user = createUser();
                String loginId = createLocalUser(user);
                AuthService service = bean(AuthService.class);

                TokenResponse first = service.localLogin(new AccountData(loginId, PASSWORD));
                TokenResponse second = service.localLogin(new AccountData(loginId, PASSWORD));

                JwtTokenProvider jwt = bean(JwtTokenProvider.class);
                assertNotEquals(jwt.getJti(first.getAccessToken()), jwt.getJti(second.getAccessToken()));
                assertEquals(Optional.of(jwt.getJti(second.getAccessToken())), accessJti(user.getId()));
            }

            @Test
            @DisplayName("없는 아이디면 AccountException")
            void unknownLoginId() {
                AccountException e = assertThrows(AccountException.class,
                        () -> bean(AuthService.class).localLogin(new AccountData(unique("nobody"), PASSWORD)));
                assertEquals(400, e.status().code());
            }

            @Test
            @DisplayName("비밀번호가 틀리면 PasswordMisMatchException이고 토큰을 만들지 않는다")
            void wrongPassword() {
                AppUser user = createUser();
                String loginId = createLocalUser(user);

                PasswordMisMatchException e = assertThrows(PasswordMisMatchException.class,
                        () -> bean(AuthService.class).localLogin(new AccountData(loginId, "wrong")));
                assertEquals(400, e.status().code());
                assertTrue(accessJti(user.getId()).isEmpty(), "실패한 로그인인데 Redis에 토큰이 생겼다");
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/auth/social-login")
    @DisplayName("POST /auth/social-login - 소셜 로그인 code를 토큰으로 교환")
    class SocialLogin {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("Redis에 있는 code면 200과 그 유저의 토큰을 돌려준다")
            void success() {
                AppUser user = createUser();
                String code = putSocialLoginCode(user.getId());

                ApiResponse response = client.post("/auth/social-login").query("code", code)
                        .send().assertStatus(200);

                assertIssuedFor(user.getId(),
                        response.json("/accessToken").asText(),
                        response.json("/refreshToken").asText());
            }

            @Test
            @DisplayName("code는 한 번만 쓸 수 있다. 두 번째는 401")
            void codeIsOneTime() {
                String code = putSocialLoginCode(createUser().getId());

                client.post("/auth/social-login").query("code", code).send().assertStatus(200);
                client.post("/auth/social-login").query("code", code).send().assertStatus(401);
            }

            @Test
            @DisplayName("모르는 code면 401")
            void unknownCode() {
                client.post("/auth/social-login").query("code", UUID.randomUUID().toString())
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("code가 없으면 422")
            void missingCode() {
                ApiResponse response = client.post("/auth/social-login").send().assertStatus(422);
                assertEquals("code", response.json("/detail/0/loc/1").asText(), response::toString);
            }

            @Test
            @DisplayName("code가 빈 문자열이면 422")
            void emptyCode() {
                client.post("/auth/social-login").query("code", "").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("code를 쓰면 Redis에서 지우고 토큰을 발급한다")
            void exchangesAndDeletesCode() {
                AppUser user = createUser();
                String code = putSocialLoginCode(user.getId());

                TokenResponse result = bean(AuthService.class).exchangeLoginCode(code);

                assertIssuedFor(user.getId(), result.getAccessToken(), result.getRefreshToken());
                assertTrue(bean(RedisUtil.class).getValue(RedisKey.SOCIAL_LOGIN, code).isEmpty(), "쓴 code가 Redis에 남아 있다");
            }

            @Test
            @DisplayName("없는 code면 UnauthorizedException")
            void unknownCode() {
                assertThrows(UnauthorizedException.class,
                        () -> bean(AuthService.class).exchangeLoginCode(UUID.randomUUID().toString()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/auth/logout")
    @DisplayName("POST /auth/logout - 로그아웃")
    class Logout {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인한 유저면 200이고 Redis의 access/refresh jti가 지워진다")
            void success() {
                AppUser user = createUser();
                String token = login(user);

                client.post("/auth/logout").bearer(token).send().assertStatus(200);

                assertTrue(accessJti(user.getId()).isEmpty(), "access jti가 남아 있다");
                assertTrue(refreshJti(user.getId()).isEmpty(), "refresh jti가 남아 있다");
            }

            @Test
            @DisplayName("[BUG] 로그아웃한 access 토큰으로는 더 이상 로그인이 필요한 API를 부를 수 없다 (401)")
            void tokenRejectedAfterLogout() {
                //BUG: CurrentUser.authenticate가 서명/만료만 보고 Redis 화이트리스트(jti)를 확인하지 않아 로그아웃한 토큰도 통과한다
                AppUser user = createUser();
                String token = login(user);
                client.post("/auth/logout").bearer(token).send().assertStatus(200);

                client.get("/user/me").bearer(token).send().assertStatus(401);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.post("/auth/logout").send().assertStatus(401);
            }

            @Test
            @DisplayName("JWT가 아닌 토큰이면 401")
            void invalidToken() {
                client.post("/auth/logout").bearer("not-a-jwt").send().assertStatus(401);
            }

            @Test
            @DisplayName("Bearer가 아닌 스킴이면 401")
            void wrongScheme() {
                client.post("/auth/logout").header("Authorization", "Basic " + login(createUser()))
                        .send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("그 유저의 access/refresh jti만 지우고 다른 유저 것은 건드리지 않는다")
            void deletesOnlyOwnTokens() {
                AppUser me = createUser();
                AppUser other = createUser();
                login(me);
                login(other);

                bean(AuthService.class).logout(me.getId());

                assertTrue(accessJti(me.getId()).isEmpty());
                assertTrue(refreshJti(me.getId()).isEmpty());
                assertTrue(accessJti(other.getId()).isPresent(), "다른 유저의 토큰까지 지워졌다");
                assertTrue(refreshJti(other.getId()).isPresent(), "다른 유저의 토큰까지 지워졌다");
            }

            @Test
            @DisplayName("로그인한 적 없는 유저를 로그아웃해도 예외가 없다")
            void logoutWithoutLogin() {
                assertDoesNotThrow(() -> bean(AuthService.class).logout(createUser().getId()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/auth/check-login-id")
    @DisplayName("GET /auth/check-login-id - 로그인 아이디 중복 확인")
    class CheckLoginId {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("아무도 안 쓰는 아이디면 available=true")
            void available() {
                ApiResponse response = client.get("/auth/check-login-id").query("login_id", unique("free"))
                        .send().assertStatus(200);
                assertTrue(response.json("/available").isBoolean(), response::toString);
                assertTrue(response.json("/available").asBoolean(), response::toString);
            }

            @Test
            @DisplayName("이미 있는 아이디면 available=false")
            void taken() {
                String loginId = createLocalUser(createUser());

                ApiResponse response = client.get("/auth/check-login-id").query("login_id", loginId)
                        .send().assertStatus(200);
                assertFalse(response.json("/available").asBoolean(true), response::toString);
            }

            @Test
            @DisplayName("login_id가 없으면 422")
            void missingLoginId() {
                ApiResponse response = client.get("/auth/check-login-id").send().assertStatus(422);
                assertEquals("login_id", response.json("/detail/0/loc/1").asText(), response::toString);
            }

            @Test
            @DisplayName("camelCase(loginId)로 보내면 login_id가 없는 것과 같아 422")
            void wrongQueryName() {
                client.get("/auth/check-login-id").query("loginId", unique("free")).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("없는 아이디는 true, 있는 아이디는 false")
            void checksExistence() {
                String loginId = createLocalUser(createUser());
                LocalAccountService service = bean(LocalAccountService.class);

                assertFalse(service.checkDuplicateLoginId(loginId).isAvailable());
                assertTrue(service.checkDuplicateLoginId(unique("free")).isAvailable());
            }
        }
    }
}
