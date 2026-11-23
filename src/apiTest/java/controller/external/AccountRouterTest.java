package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import constant.Role;
import constant.SocialProvider;
import exception.AlreadyExistException;
import exception.InvalidRequestException;
import exception.NotFoundException;
import exception.PasswordMisMatchException;
import exception.UnauthorizedException;
import fixture.AccountFixtures;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.domain.SocialAccount;
import model.dto.SocialUserInfo;
import model.dto.request.account.ChangePasswordRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.MyRoleResponse;
import model.dto.response.account.MySocialAccountResponse;
import model.dto.response.account.SocialAccountResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.LocalAccountRepository;
import repository.SocialAccountRepository;
import repository.UserRepository;
import service.OAuthClient;
import service.external.LocalAccountService;
import service.external.SocialAccountService;
import service.external.UserRoleService;
import service.external.UserService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;
import util.AccountUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountRouter (/account)")
class AccountRouterTest extends ApiTest {
    private static final String PASSWORD = "password1234!";
    private static final String NEW_PASSWORD = "newPassword5678!";

    private LocalAccount createLocalAccount(AppUser user) {
        return AccountFixtures.createLocalAccount(user, unique("login"), PASSWORD);
    }

    private LocalAccount reloadAccount(LocalAccount account) {
        return bean(LocalAccountRepository.class).findById(account.getId()).orElseThrow();
    }

    private boolean passwordMatches(LocalAccount account, String rawPassword) throws Exception {
        LocalAccount saved = reloadAccount(account);
        return bean(AccountUtil.class).matches(rawPassword, saved.getSalt(), saved.getPassword());
    }

    /** 외부 소셜 서버 대신 정해진 유저 정보를 돌려주는 OAuthClient. 카카오/구글에 실제로 요청하지 않는다. */
    private static final class FakeOAuthClient extends OAuthClient {
        private final String providerUserId;
        private String receivedCode;

        FakeOAuthClient(String providerUserId) {
            super(null, null, "", "", "");
            this.providerUserId = providerUserId;
        }

        @Override
        public String exchangeCode(SocialProvider socialProvider, String code) {
            this.receivedCode = code;
            return "{\"access_token\":\"fake-access-token\"}";
        }

        @Override
        public SocialUserInfo fetchUserInfo(SocialProvider socialProvider, String exchangedCode) {
            return SocialUserInfo.builder()
                    .providerUserId(this.providerUserId)
                    .email(this.providerUserId + "@social.test")
                    .name("소셜 " + this.providerUserId)
                    .build();
        }
    }

    /** 앱과 같은 리포지토리를 쓰되 OAuthClient만 가짜로 바꾼 SocialAccountService */
    private SocialAccountService socialAccountServiceWith(OAuthClient oAuthClient) {
        return new SocialAccountService(bean(SocialAccountRepository.class), bean(UserRepository.class), oAuthClient);
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/account/{local_account_id}")
    @DisplayName("DELETE /account/{local_account_id} - 회원 탈퇴")
    class DeleteLocalAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("내 로컬 계정이면 200, result=true이고 유저와 계정이 지워진다")
            void success() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);
                SocialAccount social = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);

                ApiResponse response = client.delete("/account/" + account.getId()).bearer(login(user))
                        .send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(false), response::toString);
                assertTrue(bean(UserRepository.class).findById(user.getId()).isEmpty(), "유저가 남아 있다");
                assertTrue(bean(LocalAccountRepository.class).findById(account.getId()).isEmpty(), "로컬 계정이 남아 있다");
                assertTrue(bean(SocialAccountRepository.class).findById(social.getId()).isEmpty(), "소셜 계정이 남아 있다");
            }

            @Test
            @DisplayName("탈퇴 후에는 그 아이디로 로그인할 수 없다")
            void cannotLoginAfterDelete() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                client.delete("/account/" + account.getId()).bearer(login(user)).send().assertStatus(200);

                client.post("/auth/local-login")
                        .json(Map.of("loginId", account.getLoginId(), "password", PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("남의 로컬 계정이면 404이고 아무것도 지워지지 않는다")
            void othersAccount() {
                AppUser me = createUser();
                createLocalAccount(me);
                AppUser other = createUser();
                LocalAccount othersAccount = createLocalAccount(other);

                client.delete("/account/" + othersAccount.getId()).bearer(login(me)).send().assertStatus(404);

                assertTrue(bean(UserRepository.class).findById(other.getId()).isPresent());
                assertTrue(bean(UserRepository.class).findById(me.getId()).isPresent());
                assertTrue(bean(LocalAccountRepository.class).findById(othersAccount.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 로컬 계정 id면 404")
            void notFound() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);
                bean(LocalAccountRepository.class).delete(account.getId());

                client.delete("/account/" + account.getId()).bearer(login(user)).send().assertStatus(404);
                assertTrue(bean(UserRepository.class).findById(user.getId()).isPresent());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                LocalAccount account = createLocalAccount(createUser());

                client.delete("/account/" + account.getId()).send().assertStatus(401);
                assertTrue(bean(LocalAccountRepository.class).findById(account.getId()).isPresent());
            }

            @Test
            @DisplayName("local_account_id가 숫자가 아니면 422")
            void notNumber() {
                ApiResponse response = client.delete("/account/abc").bearer(login(createUser()))
                        .send().assertStatus(422);
                assertEquals("local_account_id", response.json("/detail/0/loc/1").asText(), response::toString);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("내 계정이면 유저를 지우고 로컬 계정도 함께 지워진다")
            void deletes() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                DeleteResponse result = bean(UserService.class).deleteLocalAccount(user.getId(), account.getId());

                assertTrue(result.getResult());
                assertTrue(bean(UserRepository.class).findById(user.getId()).isEmpty());
                assertTrue(bean(LocalAccountRepository.class).findById(account.getId()).isEmpty());
            }

            @Test
            @DisplayName("남의 계정이면 NotFoundException")
            void othersAccount() {
                AppUser me = createUser();
                LocalAccount othersAccount = createLocalAccount(createUser());

                assertThrows(NotFoundException.class,
                        () -> bean(UserService.class).deleteLocalAccount(me.getId(), othersAccount.getId()));
                assertTrue(bean(LocalAccountRepository.class).findById(othersAccount.getId()).isPresent());
                assertTrue(bean(UserRepository.class).findById(me.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 계정이면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);
                bean(LocalAccountRepository.class).delete(account.getId());

                assertThrows(NotFoundException.class,
                        () -> bean(UserService.class).deleteLocalAccount(user.getId(), account.getId()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/account/change-password")
    @DisplayName("PATCH /account/change-password - 비밀번호 변경")
    class ChangePassword {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("현재 비밀번호가 맞으면 200과 로컬 계정 id를 돌려주고, 새 비밀번호로만 로그인된다")
            void success() {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                ApiResponse response = client.patch("/account/change-password").bearer(login(user))
                        .json(Map.of("currentPassword", PASSWORD, "newPassword", NEW_PASSWORD))
                        .send().assertStatus(200);
                assertEquals(account.getId(), response.json("/id").asLong(), response::toString);

                client.post("/auth/local-login")
                        .json(Map.of("loginId", account.getLoginId(), "password", NEW_PASSWORD))
                        .send().assertStatus(200);
                client.post("/auth/local-login")
                        .json(Map.of("loginId", account.getLoginId(), "password", PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("현재 비밀번호가 틀리면 400이고 비밀번호가 바뀌지 않는다")
            void wrongCurrentPassword() throws Exception {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                client.patch("/account/change-password").bearer(login(user))
                        .json(Map.of("currentPassword", "wrong", "newPassword", NEW_PASSWORD))
                        .send().assertStatus(400);

                assertTrue(passwordMatches(account, PASSWORD));
            }

            @Test
            @DisplayName("[BUG] 새 비밀번호가 현재 비밀번호와 같으면 400")
            void samePassword() {
                //BUG: InvalidRequestException(String) 생성자가 400(STATUS_CODE)을 넘기지 않아 기본값 500으로 응답한다
                AppUser user = createUser();
                createLocalAccount(user);

                client.patch("/account/change-password").bearer(login(user))
                        .json(Map.of("currentPassword", PASSWORD, "newPassword", PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("[BUG] 새 비밀번호가 빠지면 400")
            void missingNewPassword() {
                //BUG: InvalidRequestException(String) 생성자가 400(STATUS_CODE)을 넘기지 않아 기본값 500으로 응답한다
                AppUser user = createUser();
                createLocalAccount(user);

                client.patch("/account/change-password").bearer(login(user))
                        .json(Map.of("currentPassword", PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("[BUG] 로컬 계정이 없는 유저(소셜 전용)면 400")
            void noLocalAccount() {
                //BUG: InvalidRequestException(String) 생성자가 400(STATUS_CODE)을 넘기지 않아 기본값 500으로 응답한다
                AppUser user = createUser();

                client.patch("/account/change-password").bearer(login(user))
                        .json(Map.of("currentPassword", PASSWORD, "newPassword", NEW_PASSWORD))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("토큰의 유저가 사라졌으면 401")
            void userDeleted() {
                AppUser user = createUser();
                String token = login(user);
                bean(UserRepository.class).delete(user.getId());

                client.patch("/account/change-password").bearer(token)
                        .json(Map.of("currentPassword", PASSWORD, "newPassword", NEW_PASSWORD))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.patch("/account/change-password")
                        .json(Map.of("currentPassword", PASSWORD, "newPassword", NEW_PASSWORD))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.patch("/account/change-password").bearer(login(createUser())).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("새 salt로 새 비밀번호를 해시해서 저장한다")
            void changesPasswordAndSalt() throws Exception {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                IdResponse result = bean(LocalAccountService.class).changePassword(user.getId(),
                        new ChangePasswordRequest(PASSWORD, NEW_PASSWORD));

                assertEquals(account.getId(), result.getId());
                LocalAccount updated = reloadAccount(account);
                assertNotEquals(account.getSalt(), updated.getSalt(), "salt가 바뀌지 않았다");
                assertNotEquals(NEW_PASSWORD, updated.getPassword(), "비밀번호가 평문으로 저장됐다");
                assertTrue(passwordMatches(account, NEW_PASSWORD));
                assertFalse(passwordMatches(account, PASSWORD));
            }

            @Test
            @DisplayName("현재 비밀번호가 틀리면 PasswordMisMatchException(400)")
            void wrongCurrentPassword() throws Exception {
                AppUser user = createUser();
                LocalAccount account = createLocalAccount(user);

                PasswordMisMatchException e = assertThrows(PasswordMisMatchException.class,
                        () -> bean(LocalAccountService.class).changePassword(user.getId(),
                                new ChangePasswordRequest("wrong", NEW_PASSWORD)));
                assertEquals(400, e.status().code());
                assertTrue(passwordMatches(account, PASSWORD));
            }

            @Test
            @DisplayName("같은 비밀번호, 빈 값이면 InvalidRequestException")
            void invalidInput() {
                AppUser user = createUser();
                createLocalAccount(user);
                LocalAccountService service = bean(LocalAccountService.class);

                assertThrows(InvalidRequestException.class,
                        () -> service.changePassword(user.getId(), new ChangePasswordRequest(PASSWORD, PASSWORD)));
                assertThrows(InvalidRequestException.class,
                        () -> service.changePassword(user.getId(), new ChangePasswordRequest(PASSWORD, " ")));
                assertThrows(InvalidRequestException.class,
                        () -> service.changePassword(user.getId(), new ChangePasswordRequest(null, NEW_PASSWORD)));
            }

            @Test
            @DisplayName("로컬 계정이 없으면 InvalidRequestException")
            void noLocalAccount() {
                AppUser user = createUser();

                assertThrows(InvalidRequestException.class, () -> bean(LocalAccountService.class)
                        .changePassword(user.getId(), new ChangePasswordRequest(PASSWORD, NEW_PASSWORD)));
            }

            @Test
            @DisplayName("유저가 없으면 UnauthorizedException")
            void userMissing() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                assertThrows(UnauthorizedException.class, () -> bean(LocalAccountService.class)
                        .changePassword(user.getId(), new ChangePasswordRequest(PASSWORD, NEW_PASSWORD)));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/account/social")
    @DisplayName("GET /account/social - 내 소셜 계정 조회")
    class GetMySocialAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("연동한 소셜 계정만 200으로 돌려준다")
            void success() {
                AppUser user = createUser();
                SocialAccount google = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);
                SocialAccount kakao = AccountFixtures.createSocialAccount(user, SocialProvider.KAKAO);
                AccountFixtures.createSocialAccount(createUser(), SocialProvider.GOOGLE);

                ApiResponse response = client.get("/account/social").bearer(login(user)).send().assertStatus(200);

                JsonNode accounts = response.json("/socialAccounts");
                assertTrue(accounts.isArray(), response::toString);
                assertEquals(2, accounts.size(), response::toString);
                Map<Long, String> byId = StreamSupport.stream(accounts.spliterator(), false)
                        .collect(Collectors.toMap(n -> n.path("socialAccountId").asLong(), n -> n.path("socialProvider").asText()));
                assertEquals(Map.of(google.getId(), "GOOGLE", kakao.getId(), "KAKAO"), byId);
                accounts.forEach(node -> assertEquals(user.getId(), node.path("userId").asLong(), response::toString));
            }

            @Test
            @DisplayName("연동한 계정이 없으면 빈 목록")
            void empty() {
                ApiResponse response = client.get("/account/social").bearer(login(createUser())).send().assertStatus(200);

                assertTrue(response.json("/socialAccounts").isArray(), response::toString);
                assertEquals(0, response.json("/socialAccounts").size(), response::toString);
            }

            @Test
            @DisplayName("토큰의 유저가 사라졌으면 401")
            void userDeleted() {
                AppUser user = createUser();
                String token = login(user);
                bean(UserRepository.class).delete(user.getId());

                client.get("/account/social").bearer(token).send().assertStatus(401);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/account/social").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("그 유저의 소셜 계정을 id, 유저 id, 제공자로 옮긴다")
            void mapsAccounts() {
                AppUser user = createUser();
                SocialAccount naver = AccountFixtures.createSocialAccount(user, SocialProvider.NAVER);

                MySocialAccountResponse result = bean(SocialAccountService.class).getMySocialAccount(user.getId());

                assertEquals(1, result.getSocialAccounts().size());
                SocialAccountResponse account = result.getSocialAccounts().getFirst();
                assertEquals(naver.getId(), account.getSocialAccountId());
                assertEquals(user.getId(), account.getUserId());
                assertEquals(SocialProvider.NAVER, account.getSocialProvider());
            }

            @Test
            @DisplayName("없는 유저면 UnauthorizedException")
            void userMissing() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                assertThrows(UnauthorizedException.class,
                        () -> bean(SocialAccountService.class).getMySocialAccount(user.getId()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/account/social/{provider}")
    @DisplayName("POST /account/social/{provider} - 소셜 계정 연동")
    class LinkSocialAccount {

        //성공 경로는 실제 소셜 서버(카카오/구글)에 code를 보내야 하므로 Http에서는 서버까지 가지 않는 실패만 본다
        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.post("/account/social/GOOGLE").query("code", "abc").send().assertStatus(401);
            }

            @Test
            @DisplayName("없는 제공자면 422")
            void unknownProvider() {
                ApiResponse response = client.post("/account/social/FACEBOOK").bearer(login(createUser()))
                        .query("code", "abc")
                        .send().assertStatus(422);
                assertEquals("enum", response.json("/detail/0/type").asText(), response::toString);
                assertEquals("provider", response.json("/detail/0/loc/1").asText(), response::toString);
            }

            @Test
            @DisplayName("code가 없으면 422")
            void missingCode() {
                ApiResponse response = client.post("/account/social/GOOGLE").bearer(login(createUser()))
                        .send().assertStatus(422);
                assertEquals("code", response.json("/detail/0/loc/1").asText(), response::toString);
            }
        }

        //소셜 서버 대신 가짜 OAuthClient를 넣은 서비스로 연동 분기를 검증한다
        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("소셜 계정을 만들어 유저에게 연결한다")
            void links() {
                AppUser user = createUser();
                String providerUserId = AccountFixtures.uniqueProviderUserId();
                FakeOAuthClient oAuthClient = new FakeOAuthClient(providerUserId);

                SocialAccountResponse result = socialAccountServiceWith(oAuthClient)
                        .linkSocialAccount(user.getId(), "auth-code", SocialProvider.KAKAO);

                assertEquals("auth-code", oAuthClient.receivedCode);
                assertEquals(user.getId(), result.getUserId());
                assertEquals(SocialProvider.KAKAO, result.getSocialProvider());
                SocialAccount saved = bean(SocialAccountRepository.class).findById(result.getSocialAccountId()).orElseThrow();
                assertEquals(user.getId(), saved.getAppUser().getId());
                assertEquals(SocialProvider.KAKAO, saved.getSocialProvider());
                assertEquals(providerUserId, saved.getSocialProviderUserId());
            }

            @Test
            @DisplayName("같은 소셜 계정이 이미 다른 유저에게 연동돼 있으면 AlreadyExistException")
            void identityTaken() {
                String providerUserId = AccountFixtures.uniqueProviderUserId();
                SocialAccountService service = socialAccountServiceWith(new FakeOAuthClient(providerUserId));
                service.linkSocialAccount(createUser().getId(), "code", SocialProvider.GOOGLE);
                AppUser me = createUser();

                AlreadyExistException e = assertThrows(AlreadyExistException.class,
                        () -> service.linkSocialAccount(me.getId(), "code", SocialProvider.GOOGLE));
                assertEquals(409, e.status().code());
                assertTrue(bean(SocialAccountRepository.class).findByUserId(me.getId()).isEmpty());
            }

            @Test
            @DisplayName("이미 같은 제공자 계정을 연동한 유저면 AlreadyExistException")
            void providerAlreadyLinked() {
                AppUser user = createUser();
                AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);
                SocialAccountService service = socialAccountServiceWith(new FakeOAuthClient(AccountFixtures.uniqueProviderUserId()));

                assertThrows(AlreadyExistException.class,
                        () -> service.linkSocialAccount(user.getId(), "code", SocialProvider.GOOGLE));
                assertEquals(1, bean(SocialAccountRepository.class).findByUserId(user.getId()).size());
            }

            @Test
            @DisplayName("다른 제공자라면 하나 더 연동할 수 있다")
            void anotherProvider() {
                AppUser user = createUser();
                AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);
                SocialAccountService service = socialAccountServiceWith(new FakeOAuthClient(AccountFixtures.uniqueProviderUserId()));

                service.linkSocialAccount(user.getId(), "code", SocialProvider.NAVER);

                Set<SocialProvider> providers = bean(SocialAccountRepository.class).findByUserId(user.getId()).stream()
                        .map(SocialAccount::getSocialProvider)
                        .collect(Collectors.toSet());
                assertEquals(Set.of(SocialProvider.GOOGLE, SocialProvider.NAVER), providers);
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void userMissing() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());
                SocialAccountService service = socialAccountServiceWith(new FakeOAuthClient(AccountFixtures.uniqueProviderUserId()));

                assertThrows(NotFoundException.class,
                        () -> service.linkSocialAccount(user.getId(), "code", SocialProvider.GOOGLE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/account/social/{provider}/{social_account_id}")
    @DisplayName("DELETE /account/social/{provider}/{social_account_id} - 소셜 계정 연동 해제")
    class UnlinkSocialAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("내 소셜 계정이면 200, result=true이고 DB에서 지워진다")
            void success() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.KAKAO);

                ApiResponse response = client.delete("/account/social/KAKAO/" + account.getId()).bearer(login(user))
                        .send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(false), response::toString);
                assertTrue(bean(SocialAccountRepository.class).findById(account.getId()).isEmpty());
                assertTrue(bean(UserRepository.class).findById(user.getId()).isPresent(), "유저까지 지워졌다");
            }

            @Test
            @DisplayName("제공자 이름은 대소문자를 가리지 않는다 (kakao)")
            void lowerCaseProvider() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.KAKAO);

                client.delete("/account/social/kakao/" + account.getId()).bearer(login(user)).send().assertStatus(200);
            }

            @Test
            @DisplayName("남의 소셜 계정이면 404이고 지워지지 않는다")
            void othersAccount() {
                SocialAccount othersAccount = AccountFixtures.createSocialAccount(createUser(), SocialProvider.GOOGLE);

                client.delete("/account/social/GOOGLE/" + othersAccount.getId()).bearer(login(createUser()))
                        .send().assertStatus(404);
                assertTrue(bean(SocialAccountRepository.class).findById(othersAccount.getId()).isPresent());
            }

            @Test
            @DisplayName("경로의 제공자가 계정의 제공자와 다르면 404이고 지워지지 않는다")
            void providerMismatch() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);

                client.delete("/account/social/NAVER/" + account.getId()).bearer(login(user)).send().assertStatus(404);
                assertTrue(bean(SocialAccountRepository.class).findById(account.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 소셜 계정 id면 404")
            void notFound() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);
                bean(SocialAccountRepository.class).delete(account.getId());

                client.delete("/account/social/GOOGLE/" + account.getId()).bearer(login(user)).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                SocialAccount account = AccountFixtures.createSocialAccount(createUser(), SocialProvider.GOOGLE);

                client.delete("/account/social/GOOGLE/" + account.getId()).send().assertStatus(401);
                assertTrue(bean(SocialAccountRepository.class).findById(account.getId()).isPresent());
            }

            @Test
            @DisplayName("social_account_id가 숫자가 아니면 422")
            void notNumber() {
                ApiResponse response = client.delete("/account/social/GOOGLE/abc").bearer(login(createUser()))
                        .send().assertStatus(422);
                assertEquals("social_account_id", response.json("/detail/0/loc/1").asText(), response::toString);
            }

            @Test
            @DisplayName("없는 제공자면 422")
            void unknownProvider() {
                ApiResponse response = client.delete("/account/social/FACEBOOK/1").bearer(login(createUser()))
                        .send().assertStatus(422);
                assertEquals("provider", response.json("/detail/0/loc/1").asText(), response::toString);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("내 계정이고 제공자가 맞으면 지운다")
            void unlinks() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);

                DeleteResponse result = bean(SocialAccountService.class)
                        .unlinkedSocialAccount(user.getId(), account.getId(), SocialProvider.GOOGLE);

                assertTrue(result.getResult());
                assertTrue(bean(SocialAccountRepository.class).findById(account.getId()).isEmpty());
            }

            @Test
            @DisplayName("남의 계정이면 NotFoundException")
            void othersAccount() {
                SocialAccount othersAccount = AccountFixtures.createSocialAccount(createUser(), SocialProvider.GOOGLE);
                AppUser me = createUser();

                assertThrows(NotFoundException.class, () -> bean(SocialAccountService.class)
                        .unlinkedSocialAccount(me.getId(), othersAccount.getId(), SocialProvider.GOOGLE));
                assertTrue(bean(SocialAccountRepository.class).findById(othersAccount.getId()).isPresent());
            }

            @Test
            @DisplayName("제공자가 다르면 NotFoundException")
            void providerMismatch() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);

                assertThrows(NotFoundException.class, () -> bean(SocialAccountService.class)
                        .unlinkedSocialAccount(user.getId(), account.getId(), SocialProvider.KAKAO));
                assertTrue(bean(SocialAccountRepository.class).findById(account.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 계정이면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                SocialAccount account = AccountFixtures.createSocialAccount(user, SocialProvider.GOOGLE);
                bean(SocialAccountRepository.class).delete(account.getId());

                assertThrows(NotFoundException.class, () -> bean(SocialAccountService.class)
                        .unlinkedSocialAccount(user.getId(), account.getId(), SocialProvider.GOOGLE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/account/role")
    @DisplayName("GET /account/role - 내 권한 조회")
    class GetMyRole {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 내가 가진 권한 이름 목록을 돌려준다")
            void success() {
                AppUser user = createUser();
                AccountFixtures.grantRole(user, Role.USER);
                AccountFixtures.grantRole(user, Role.MAKER);

                ApiResponse response = client.get("/account/role").bearer(login(user)).send().assertStatus(200);

                JsonNode roles = response.json("/role");
                assertTrue(roles.isArray(), response::toString);
                Set<String> names = StreamSupport.stream(roles.spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toSet());
                assertEquals(Set.of("USER", "MAKER"), names);
            }

            @Test
            @DisplayName("권한이 없으면 빈 목록")
            void empty() {
                ApiResponse response = client.get("/account/role").bearer(login(createUser())).send().assertStatus(200);

                assertTrue(response.json("/role").isArray(), response::toString);
                assertEquals(0, response.json("/role").size(), response::toString);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/account/role").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("그 유저에게 연결된 권한만 돌려준다")
            void ownRolesOnly() {
                AppUser user = createUser();
                AppUser admin = createUser();
                AccountFixtures.grantRole(user, Role.USER);
                AccountFixtures.grantRole(admin, Role.ADMIN);

                MyRoleResponse result = bean(UserRoleService.class).getCurrentUserPermissions(user.getId());

                assertEquals(List.of(Role.USER), result.getRole());
            }

            @Test
            @DisplayName("권한이 없으면 빈 목록")
            void noRoles() {
                MyRoleResponse result = bean(UserRoleService.class).getCurrentUserPermissions(createUser().getId());

                assertTrue(result.getRole().isEmpty());
            }
        }
    }
}
