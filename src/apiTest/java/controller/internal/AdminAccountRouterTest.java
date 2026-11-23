package controller.internal;

import constant.SocialProvider;
import exception.NotFoundException;
import fixture.AdminUserFixtures;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.domain.SocialAccount;
import model.dto.response.account.LocalAccountResponse;
import model.dto.response.account.SocialAccountResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.SocialAccountRepository;
import repository.UserRepository;
import service.internal.AdminLocalAccountService;
import service.internal.AdminSocialAccountService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import static fixture.AdminUserFixtures.MISSING_ID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminAccountRouter (/internal/account)")
class AdminAccountRouterTest extends ApiTest {

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/account/social/{social_account_id}")
    @DisplayName("GET /internal/account/social/{social_account_id} - 소셜 계정 단건 조회")
    class GetSocialAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 소셜 계정 id, 유저 id, 제공자를 돌려준다")
            void returnsAccount() {
                AppUser user = createUser();
                SocialAccount account = AdminUserFixtures.socialAccount(user, SocialProvider.KAKAO);

                ApiResponse response = client.get("/internal/account/social/" + account.getId()).send().assertStatus(200);

                assertEquals(account.getId(), response.json("/socialAccountId").asLong());
                assertEquals(user.getId(), response.json("/userId").asLong());
                assertEquals("KAKAO", response.json("/socialProvider").asText(), response::toString);
            }

            @Test
            @DisplayName("없는 소셜 계정이면 404")
            void notFound() {
                client.get("/internal/account/social/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("social_account_id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/account/social/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("한 유저가 가진 여러 소셜 계정을 각각 구분해서 돌려준다")
            void distinguishesAccounts() {
                AppUser user = createUser();
                SocialAccount google = AdminUserFixtures.socialAccount(user, SocialProvider.GOOGLE);
                SocialAccount naver = AdminUserFixtures.socialAccount(user, SocialProvider.NAVER);
                AdminSocialAccountService service = bean(AdminSocialAccountService.class);

                SocialAccountResponse g = service.getSocialAccount(google.getId());
                SocialAccountResponse n = service.getSocialAccount(naver.getId());

                assertEquals(SocialProvider.GOOGLE, g.getSocialProvider());
                assertEquals(SocialProvider.NAVER, n.getSocialProvider());
                assertEquals(user.getId(), g.getUserId());
                assertEquals(user.getId(), n.getUserId());
                assertEquals(google.getId(), g.getSocialAccountId());
            }

            @Test
            @DisplayName("없는 소셜 계정이면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminSocialAccountService.class).getSocialAccount(MISSING_ID));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/account/social/{social_account_id}")
    @DisplayName("DELETE /internal/account/social/{social_account_id} - 소셜 계정 삭제")
    class DeleteSocialAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                SocialAccount account = AdminUserFixtures.socialAccount(createUser(), SocialProvider.GOOGLE);

                ApiResponse response = client.delete("/internal/account/social/" + account.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/account/social/" + account.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 소셜 계정이면 404")
            void notFound() {
                client.delete("/internal/account/social/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("social_account_id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/account/social/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("소셜 계정만 지우고 유저와 다른 소셜 계정은 남긴다")
            void keepsUserAndOthers() {
                AppUser user = createUser();
                SocialAccount target = AdminUserFixtures.socialAccount(user, SocialProvider.GOOGLE);
                SocialAccount other = AdminUserFixtures.socialAccount(user, SocialProvider.KAKAO);

                assertTrue(bean(AdminSocialAccountService.class).deleteSocialAccount(target.getId()).getResult());

                SocialAccountRepository repository = bean(SocialAccountRepository.class);
                assertFalse(repository.isExistById(target.getId()));
                assertTrue(repository.isExistById(other.getId()));
                assertTrue(bean(UserRepository.class).isExistById(user.getId()));
            }

            @Test
            @DisplayName("두 번 삭제하면 두 번째는 NotFoundException")
            void deleteTwice() {
                SocialAccount account = AdminUserFixtures.socialAccount(createUser(), SocialProvider.NAVER);
                AdminSocialAccountService service = bean(AdminSocialAccountService.class);

                service.deleteSocialAccount(account.getId());
                assertThrows(NotFoundException.class, () -> service.deleteSocialAccount(account.getId()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/account/local/{local_account_id}")
    @DisplayName("GET /internal/account/local/{local_account_id} - 로컬 계정 단건 조회")
    class GetLocalAccount {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 로컬 계정 id, 유저 id, 로그인 id를 돌려준다")
            void returnsAccount() {
                AppUser user = createUser();
                LocalAccount account = AdminUserFixtures.localAccount(user);

                ApiResponse response = client.get("/internal/account/local/" + account.getId()).send().assertStatus(200);

                assertEquals(account.getId(), response.json("/localAccountId").asLong());
                assertEquals(user.getId(), response.json("/userId").asLong());
                assertEquals(account.getLoginId(), response.json("/loginId").asText());
            }

            @Test
            @DisplayName("비밀번호와 salt는 응답에 없다")
            void hidesSecrets() {
                LocalAccount account = AdminUserFixtures.localAccount(createUser());

                ApiResponse response = client.get("/internal/account/local/" + account.getId()).send().assertStatus(200);

                assertTrue(response.json().path("password").isMissingNode(), response::toString);
                assertTrue(response.json().path("salt").isMissingNode(), response::toString);
                assertFalse(response.body().contains(account.getPassword()), response::toString);
            }

            @Test
            @DisplayName("없는 로컬 계정이면 404")
            void notFound() {
                client.get("/internal/account/local/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("local_account_id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/account/local/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("도메인 값이 응답으로 옮겨진다")
            void mapsFields() {
                AppUser user = createUser();
                LocalAccount account = AdminUserFixtures.localAccount(user);

                LocalAccountResponse result = bean(AdminLocalAccountService.class).getLocalAccount(account.getId());

                assertEquals(account.getId(), result.getLocalAccountId());
                assertEquals(user.getId(), result.getUserId());
                assertEquals(account.getLoginId(), result.getLoginId());
            }

            @Test
            @DisplayName("없는 로컬 계정이면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminLocalAccountService.class).getLocalAccount(MISSING_ID));
            }
        }
    }
}
