package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import constant.Role;
import constant.SocialProvider;
import exception.AlreadyExistException;
import exception.InvalidRequestException;
import exception.NotFoundException;
import fixture.AdminUserFixtures;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.domain.Review;
import model.domain.Reward;
import model.domain.SocialAccount;
import model.domain.UserRoleRelation;
import model.dto.request.user.AdminUpdateUserRequest;
import model.dto.response.user.AdminUserListResponse;
import model.dto.response.user.AdminUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.*;
import service.internal.AdminUserService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;
import constant.RedisKey;
import util.RedisUtil;

import java.util.Map;
import java.util.stream.StreamSupport;

import static fixture.AdminUserFixtures.MISSING_ID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminUserRouter (/internal/user)")
class AdminUserRouterTest extends ApiTest {

    private AppUser findUser(long userId) {
        return bean(UserRepository.class).findById(userId).orElseThrow();
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/user")
    @DisplayName("GET /internal/user - 유저 전체 조회")
    class GetAllUsers {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 users 목록을 돌려주고 방금 만든 유저가 들어 있다")
            void returnsUsers() {
                AppUser saved = AdminUserFixtures.fullUser();

                ApiResponse response = client.get("/internal/user").send().assertStatus(200);

                JsonNode users = response.json("/users");
                assertTrue(users.isArray(), response::toString);
                JsonNode node = StreamSupport.stream(users.spliterator(), false)
                        .filter(n -> n.path("userId").asLong() == saved.getId())
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("방금 만든 유저가 응답에 없다: " + response));
                assertEquals(saved.getNickname(), node.path("nickname").asText());
                assertEquals(saved.getEmail(), node.path("email").asText());
                assertEquals(saved.getPostal(), node.path("postalCode").asText());
            }

            @Test
            @DisplayName("limit을 주면 그 수만큼만 돌려준다")
            void limit() {
                createUser();
                createUser();

                ApiResponse response = client.get("/internal/user").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/users").size());
            }

            @Test
            @DisplayName("[BUG] limit이 0 이하면 400")
            void nonPositiveLimit() {
                // 버그: InvalidRequestException이 BAD_REQUEST가 아닌 기본 상태(500)로 만들어진다
                client.get("/internal/user").query("limit", 0).send().assertStatus(400);
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimitType() {
                client.get("/internal/user").query("limit", "abc").send().assertStatus(422);
            }

            @Test
            @DisplayName("응답에 version 같은 내부 값은 없다")
            void hidesInternalFields() {
                createUser();

                ApiResponse response = client.get("/internal/user").query("limit", 1).send().assertStatus(200);

                assertTrue(response.json("/users/0").path("version").isMissingNode(), response::toString);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit이 없으면 DB의 모든 유저를 돌려준다")
            void noLimit() {
                createUser();

                AdminUserListResponse result = bean(AdminUserService.class).getAllUsers(null);

                assertEquals(bean(UserRepository.class).findAll().size(), result.getUsers().size());
            }

            @Test
            @DisplayName("limit만큼 잘라서 돌려준다")
            void withLimit() {
                createUser();
                createUser();

                assertEquals(2, bean(AdminUserService.class).getAllUsers(2).getUsers().size());
            }

            @Test
            @DisplayName("limit이 0이나 음수면 InvalidRequestException")
            void nonPositiveLimit() {
                AdminUserService service = bean(AdminUserService.class);

                assertThrows(InvalidRequestException.class, () -> service.getAllUsers(0));
                assertThrows(InvalidRequestException.class, () -> service.getAllUsers(-1));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/user/{user_id}")
    @DisplayName("GET /internal/user/{user_id} - 유저 단건 조회")
    class GetUser {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 명세서 모양의 유저 정보를 돌려준다")
            void returnsUser() {
                AppUser saved = AdminUserFixtures.fullUser();

                ApiResponse response = client.get("/internal/user/" + saved.getId()).send().assertStatus(200);

                JsonNode json = response.json();
                assertEquals(saved.getId(), json.path("userId").asLong());
                assertEquals(saved.getName(), json.path("name").asText());
                assertEquals(saved.getNickname(), json.path("nickname").asText());
                assertEquals(saved.getEmail(), json.path("email").asText());
                assertEquals(saved.getPostal(), json.path("postalCode").asText());
                assertEquals(saved.getAddress(), json.path("address").asText());
                assertEquals(saved.getPhone(), json.path("phone").asText());
                assertFalse(json.path("createdAt").isMissingNode() || json.path("createdAt").isNull(), response::toString);
                assertTrue(json.path("postal").isMissingNode(), "도메인 필드명 postal이 아니라 postalCode로 내보내야 한다");
            }

            @Test
            @DisplayName("없는 유저면 404")
            void notFound() {
                client.get("/internal/user/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/user/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("도메인 값이 응답으로 옮겨진다 (postal -> postalCode)")
            void mapsFields() {
                AppUser saved = AdminUserFixtures.fullUser();

                AdminUserResponse result = bean(AdminUserService.class).getUser(saved.getId());

                assertEquals(saved.getId(), result.getUserId());
                assertEquals(saved.getName(), result.getName());
                assertEquals(saved.getPostal(), result.getPostalCode());
                assertEquals(saved.getPhone(), result.getPhone());
                assertNotNull(result.getCreatedAt());
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminUserService.class).getUser(MISSING_ID));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/user/{user_id}")
    @DisplayName("PATCH /internal/user/{user_id} - 유저 수정")
    class EditUser {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 유저 id를 돌려주고 보낸 값만 DB에 반영된다")
            void edits() {
                AppUser saved = AdminUserFixtures.fullUser();
                String nickname = unique("nick");
                String email = unique("mail") + "@test.com";

                ApiResponse response = client.patch("/internal/user/" + saved.getId())
                        .json(Map.of("nickname", nickname, "email", email, "postalCode", "99999"))
                        .send()
                        .assertStatus(200);

                assertEquals(saved.getId(), response.json("/id").asLong());
                AppUser updated = findUser(saved.getId());
                assertEquals(nickname, updated.getNickname());
                assertEquals(email, updated.getEmail());
                assertEquals("99999", updated.getPostal());
                assertEquals(saved.getName(), updated.getName());
                assertEquals(saved.getAddress(), updated.getAddress());
                assertEquals(saved.getPhone(), updated.getPhone());
            }

            @Test
            @DisplayName("수정 결과가 단건 조회에도 보인다")
            void visibleOnGet() {
                AppUser saved = createUser();
                String name = "바뀐 " + unique("이름");

                client.patch("/internal/user/" + saved.getId()).json(Map.of("name", name)).send().assertStatus(200);

                ApiResponse response = client.get("/internal/user/" + saved.getId()).send().assertStatus(200);
                assertEquals(name, response.json("/name").asText());
            }

            @Test
            @DisplayName("다른 유저가 쓰는 닉네임이면 409")
            void duplicateNickname() {
                AppUser saved = createUser();
                AppUser other = createUser();

                client.patch("/internal/user/" + saved.getId())
                        .json(Map.of("nickname", other.getNickname()))
                        .send()
                        .assertStatus(409);

                assertEquals(saved.getNickname(), findUser(saved.getId()).getNickname());
            }

            @Test
            @DisplayName("다른 유저가 쓰는 이메일이면 409")
            void duplicateEmail() {
                AppUser saved = createUser();
                AppUser other = createUser();

                client.patch("/internal/user/" + saved.getId())
                        .json(Map.of("email", other.getEmail()))
                        .send()
                        .assertStatus(409);

                assertEquals(saved.getEmail(), findUser(saved.getId()).getEmail());
            }

            @Test
            @DisplayName("자기 닉네임/이메일을 그대로 보내면 200")
            void sameValues() {
                AppUser saved = createUser();

                client.patch("/internal/user/" + saved.getId())
                        .json(Map.of("nickname", saved.getNickname(), "email", saved.getEmail()))
                        .send()
                        .assertStatus(200);
            }

            @Test
            @DisplayName("없는 유저면 404")
            void notFound() {
                client.patch("/internal/user/" + MISSING_ID)
                        .json(Map.of("name", "x"))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("본문이 없으면 422")
            void missingBody() {
                AppUser saved = createUser();

                client.patch("/internal/user/" + saved.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("필드 타입이 틀리면 422")
            void wrongFieldType() {
                AppUser saved = createUser();

                client.patch("/internal/user/" + saved.getId())
                        .rawJson("{\"nickname\": 123}")
                        .send()
                        .assertStatus(422);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/user/abc").json(Map.of("name", "x")).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("null인 값은 수정하지 않는다")
            void ignoresNulls() {
                AppUser saved = AdminUserFixtures.fullUser();

                bean(AdminUserService.class).editUser(saved.getId(),
                        AdminUpdateUserRequest.builder().address("부산시").build());

                AppUser updated = findUser(saved.getId());
                assertEquals("부산시", updated.getAddress());
                assertEquals(saved.getNickname(), updated.getNickname());
                assertEquals(saved.getEmail(), updated.getEmail());
                assertEquals(saved.getPostal(), updated.getPostal());
            }

            @Test
            @DisplayName("postalCode는 AppUser의 postal로 저장된다")
            void postalCodeMapsToPostal() {
                AppUser saved = createUser();

                bean(AdminUserService.class).editUser(saved.getId(),
                        AdminUpdateUserRequest.builder().postalCode("54321").build());

                assertEquals("54321", findUser(saved.getId()).getPostal());
            }

            @Test
            @DisplayName("중복 닉네임/이메일이면 AlreadyExistException")
            void duplicates() {
                AppUser saved = createUser();
                AppUser other = createUser();
                AdminUserService service = bean(AdminUserService.class);

                assertThrows(AlreadyExistException.class, () -> service.editUser(saved.getId(),
                        AdminUpdateUserRequest.builder().nickname(other.getNickname()).build()));
                assertThrows(AlreadyExistException.class, () -> service.editUser(saved.getId(),
                        AdminUpdateUserRequest.builder().email(other.getEmail()).build()));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminUserService.class).editUser(MISSING_ID,
                        AdminUpdateUserRequest.builder().name("x").build()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/user/{user_id}")
    @DisplayName("DELETE /internal/user/{user_id} - 유저 삭제")
    class DeleteUser {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                AppUser saved = createUser();

                ApiResponse response = client.delete("/internal/user/" + saved.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/user/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 유저면 404")
            void notFound() {
                client.delete("/internal/user/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/user/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("유저의 계정, 권한, 리뷰, 프로젝트가 같이 지워진다")
            void cascades() {
                AppUser user = createUser();
                SocialAccount social = AdminUserFixtures.socialAccount(user, SocialProvider.GOOGLE);
                LocalAccount local = AdminUserFixtures.localAccount(user);
                UserRoleRelation relation = AdminUserFixtures.grant(user, Role.USER);
                Reward othersReward = AdminUserFixtures.reward(createUser());
                Review review = AdminUserFixtures.review(user, othersReward, "삭제될 리뷰", "3.0");
                Reward ownReward = AdminUserFixtures.reward(user);

                assertTrue(bean(AdminUserService.class).deleteUser(user.getId()).getResult());

                assertFalse(bean(UserRepository.class).isExistById(user.getId()));
                assertFalse(bean(SocialAccountRepository.class).isExistById(social.getId()));
                assertFalse(bean(LocalAccountRepository.class).isExistById(local.getId()));
                assertFalse(bean(UserRoleRelationRepository.class).isExistById(relation.getId()));
                assertFalse(bean(ReviewRepository.class).isExistById(review.getId()));
                assertFalse(bean(ProjectRepository.class).isExistById(ownReward.getProject().getId()));
                assertFalse(bean(RewardRepository.class).isExistById(ownReward.getId()));
                assertTrue(bean(RewardRepository.class).isExistById(othersReward.getId()), "다른 유저의 리워드는 남아야 한다");
            }

            @Test
            @DisplayName("Redis에 등록된 로그인 토큰이 지워진다")
            void removesTokens() {
                AppUser user = createUser();
                login(user);
                RedisUtil redis = bean(RedisUtil.class);
                assertTrue(redis.getValue(RedisKey.ACCESS, user.getId()).isPresent());

                bean(AdminUserService.class).deleteUser(user.getId());

                assertTrue(redis.getValue(RedisKey.ACCESS, user.getId()).isEmpty());
                assertTrue(redis.getValue(RedisKey.REFRESH, user.getId()).isEmpty());
            }

            @Test
            @DisplayName("다른 유저는 지워지지 않는다")
            void keepsOthers() {
                AppUser user = createUser();
                AppUser other = createUser();

                bean(AdminUserService.class).deleteUser(user.getId());

                assertTrue(bean(UserRepository.class).isExistById(other.getId()));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminUserService.class).deleteUser(MISSING_ID));
            }
        }
    }
}
