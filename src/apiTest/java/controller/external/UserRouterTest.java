package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import constant.Role;
import exception.AlreadyExistException;
import exception.InvalidRequestException;
import exception.NotFoundException;
import fixture.AccountFixtures;
import model.domain.AppUser;
import model.domain.LocalAccount;
import model.dto.request.user.LocalSignUpRequest;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.IdResponse;
import model.dto.response.TokenResponse;
import model.dto.response.user.MyInfoResponse;
import model.dto.response.user.UserPublicResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.LocalAccountRepository;
import repository.UserRepository;
import repository.UserRoleRelationRepository;
import service.external.UserService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;
import util.AccountUtil;
import util.JwtTokenProvider;
import constant.RedisKey;
import util.RedisUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserRouter (/user)")
class UserRouterTest extends ApiTest {
    private static final String PASSWORD = "password1234!";

    @BeforeEach
    void ensureDefaultRole() {
        //회원가입은 기본 권한(USER) 데이터가 있어야 한다. 테이블이 JVM 시작 때 비어 있으므로 만들어 둔다
        AccountFixtures.ensureRole(Role.USER);
    }

    /** 회원가입 요청 body. 아이디/닉네임/이메일/전화번호는 겹치지 않게 만든다. */
    private Map<String, Object> signUpBody() {
        String key = unique("su");
        Map<String, Object> body = new HashMap<>();
        body.put("loginId", key);
        body.put("password", PASSWORD);
        body.put("name", "가입자 " + key);
        body.put("nickname", key);
        body.put("email", key + "@test.com");
        body.put("postalCode", "12345");
        body.put("address", "서울시 테스트구 " + key);
        body.put("phone", "010-" + key);
        return body;
    }

    private LocalSignUpRequest signUpRequest() {
        Map<String, Object> body = signUpBody();
        return LocalSignUpRequest.builder()
                .loginId((String) body.get("loginId"))
                .password(PASSWORD)
                .name((String) body.get("name"))
                .nickname((String) body.get("nickname"))
                .email((String) body.get("email"))
                .postalCode("12345")
                .address((String) body.get("address"))
                .phone((String) body.get("phone"))
                .build();
    }

    /** 모든 필드를 채운 유저 */
    private AppUser createFullUser() {
        String key = unique("full");
        return bean(UserRepository.class).create(AppUser.builder()
                .name("풀 " + key)
                .nickname(key)
                .email(key + "@test.com")
                .postal("54321")
                .address("부산시 " + key)
                .phone("010-" + key)
                .build());
    }

    private AppUser reload(AppUser user) {
        return bean(UserRepository.class).findById(user.getId()).orElseThrow();
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/user/local-register")
    @DisplayName("POST /user/local-register - 로컬 회원가입")
    class LocalRegister {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 토큰을 돌려주고, 그 토큰으로 /user/me를 부르면 가입한 정보가 나온다")
            void success() {
                Map<String, Object> body = signUpBody();

                ApiResponse response = client.post("/user/local-register").json(body).send().assertStatus(200);
                String accessToken = response.json("/accessToken").asText();
                assertFalse(accessToken.isBlank(), response::toString);
                assertFalse(response.json("/refreshToken").asText().isBlank(), response::toString);

                ApiResponse me = client.get("/user/me").bearer(accessToken).send().assertStatus(200);
                assertEquals(body.get("name"), me.json("/name").asText());
                assertEquals(body.get("nickname"), me.json("/nickname").asText());
                assertEquals(body.get("email"), me.json("/email").asText());
                assertEquals(body.get("postalCode"), me.json("/postalCode").asText());
                assertEquals(body.get("address"), me.json("/address").asText());
                assertEquals(body.get("phone"), me.json("/phone").asText());
            }

            @Test
            @DisplayName("가입한 아이디와 비밀번호로 바로 로그인할 수 있다")
            void canLoginAfterSignUp() {
                Map<String, Object> body = signUpBody();
                client.post("/user/local-register").json(body).send().assertStatus(200);

                client.post("/auth/local-login")
                        .json(Map.of("loginId", body.get("loginId"), "password", PASSWORD))
                        .send().assertStatus(200);
            }

            @Test
            @DisplayName("가입하면 기본 권한 USER가 붙는다 (/account/role)")
            void grantsUserRole() {
                String accessToken = client.post("/user/local-register").json(signUpBody())
                        .send().assertStatus(200)
                        .json("/accessToken").asText();

                ApiResponse roles = client.get("/account/role").bearer(accessToken).send().assertStatus(200);
                assertEquals("USER", roles.json("/role/0").asText(), roles::toString);
                assertEquals(1, roles.json("/role").size(), roles::toString);
            }

            @Test
            @DisplayName("이미 있는 아이디면 409")
            void duplicateLoginId() {
                Map<String, Object> first = signUpBody();
                client.post("/user/local-register").json(first).send().assertStatus(200);

                Map<String, Object> second = signUpBody();
                second.put("loginId", first.get("loginId"));
                client.post("/user/local-register").json(second).send().assertStatus(409);
            }

            @Test
            @DisplayName("이미 있는 닉네임이면 409")
            void duplicateNickname() {
                AppUser existing = createUser();
                Map<String, Object> body = signUpBody();
                body.put("nickname", existing.getNickname());

                client.post("/user/local-register").json(body).send().assertStatus(409);
            }

            @Test
            @DisplayName("이미 있는 이메일이면 409")
            void duplicateEmail() {
                AppUser existing = createUser();
                Map<String, Object> body = signUpBody();
                body.put("email", existing.getEmail());

                client.post("/user/local-register").json(body).send().assertStatus(409);
            }

            @Test
            @DisplayName("[BUG] 필수 값(이메일)이 빠지면 400")
            void missingRequiredField() {
                //BUG: InvalidRequestException(String) 생성자가 400(STATUS_CODE)을 넘기지 않아 기본값 500으로 응답한다
                Map<String, Object> body = signUpBody();
                body.remove("email");

                client.post("/user/local-register").json(body).send().assertStatus(400);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/user/local-register").send().assertStatus(422);
            }

            @Test
            @DisplayName("JSON이 아닌 Content-Type이면 422")
            void wrongContentType() {
                client.post("/user/local-register")
                        .rawJson("{}")
                        .header("Content-Type", "text/plain")
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("유저, 로컬 계정(해시된 비밀번호), USER 권한을 만들고 토큰을 Redis에 등록한다")
            void createsUserAccountAndRole() throws Exception {
                LocalSignUpRequest request = signUpRequest();

                TokenResponse result = bean(UserService.class).localSignUp(request);

                long userId = bean(JwtTokenProvider.class).getUserId(result.getAccessToken());
                AppUser user = bean(UserRepository.class).findById(userId).orElseThrow();
                assertEquals(request.getName(), user.getName());
                assertEquals(request.getNickname(), user.getNickname());
                assertEquals(request.getEmail(), user.getEmail());
                assertEquals(request.getPostalCode(), user.getPostal());
                assertEquals(request.getAddress(), user.getAddress());
                assertEquals(request.getPhone(), user.getPhone());

                LocalAccount account = bean(LocalAccountRepository.class).findByLoginId(request.getLoginId()).orElseThrow();
                assertEquals(userId, account.getUser().getId());
                assertNotEquals(PASSWORD, account.getPassword(), "비밀번호가 평문으로 저장됐다");
                assertTrue(bean(AccountUtil.class).matches(PASSWORD, account.getSalt(), account.getPassword()));

                List<Role> roles = bean(UserRoleRelationRepository.class).findByUserId(userId).stream()
                        .map(r -> r.getRole().getRole())
                        .toList();
                assertEquals(List.of(Role.USER), roles);

                JwtTokenProvider jwt = bean(JwtTokenProvider.class);
                assertEquals(Optional.of(jwt.getJti(result.getAccessToken())),
                        bean(RedisUtil.class).getValue(RedisKey.ACCESS, userId));
            }

            @Test
            @DisplayName("아이디가 겹치면 AlreadyExistException이고 유저를 만들지 않는다")
            void duplicateLoginIdRollsBack() {
                LocalSignUpRequest first = signUpRequest();
                bean(UserService.class).localSignUp(first);

                LocalSignUpRequest second = LocalSignUpRequest.builder()
                        .loginId(first.getLoginId())
                        .password(PASSWORD)
                        .name("두번째")
                        .nickname(unique("second"))
                        .email(unique("second") + "@test.com")
                        .build();

                AlreadyExistException e = assertThrows(AlreadyExistException.class,
                        () -> bean(UserService.class).localSignUp(second));
                assertEquals(409, e.status().code());
                assertFalse(bean(UserRepository.class).isExistNickname(second.getNickname()), "실패한 가입인데 유저가 생겼다");
            }

            @Test
            @DisplayName("닉네임이 겹치면 AlreadyExistException")
            void duplicateNickname() {
                AppUser existing = createUser();
                LocalSignUpRequest request = LocalSignUpRequest.builder()
                        .loginId(unique("login"))
                        .password(PASSWORD)
                        .name("이름")
                        .nickname(existing.getNickname())
                        .email(unique("mail") + "@test.com")
                        .build();

                assertThrows(AlreadyExistException.class, () -> bean(UserService.class).localSignUp(request));
                assertFalse(bean(LocalAccountRepository.class).isExistLoginId(request.getLoginId()));
            }

            @Test
            @DisplayName("이메일이 겹치면 AlreadyExistException")
            void duplicateEmail() {
                AppUser existing = createUser();
                LocalSignUpRequest request = LocalSignUpRequest.builder()
                        .loginId(unique("login"))
                        .password(PASSWORD)
                        .name("이름")
                        .nickname(unique("nick"))
                        .email(existing.getEmail())
                        .build();

                assertThrows(AlreadyExistException.class, () -> bean(UserService.class).localSignUp(request));
                assertFalse(bean(LocalAccountRepository.class).isExistLoginId(request.getLoginId()));
            }

            @Test
            @DisplayName("필수 값이 비어 있으면 InvalidRequestException이고 아무것도 만들지 않는다")
            void blankRequiredField() {
                LocalSignUpRequest request = LocalSignUpRequest.builder()
                        .loginId(unique("login"))
                        .password("   ")
                        .name("이름")
                        .nickname(unique("nick"))
                        .email(unique("mail") + "@test.com")
                        .build();

                assertThrows(InvalidRequestException.class, () -> bean(UserService.class).localSignUp(request));
                assertFalse(bean(LocalAccountRepository.class).isExistLoginId(request.getLoginId()));
                assertFalse(bean(UserRepository.class).isExistNickname(request.getNickname()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/user/check-email")
    @DisplayName("GET /user/check-email - 이메일 중복 확인")
    class CheckEmail {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("아무도 안 쓰는 이메일이면 available=true")
            void available() {
                ApiResponse response = client.get("/user/check-email").query("email", unique("free") + "@test.com")
                        .send().assertStatus(200);
                assertTrue(response.json("/available").asBoolean(false), response::toString);
            }

            @Test
            @DisplayName("이미 있는 이메일이면 available=false")
            void taken() {
                AppUser user = createUser();

                ApiResponse response = client.get("/user/check-email").query("email", user.getEmail())
                        .send().assertStatus(200);
                assertFalse(response.json("/available").asBoolean(true), response::toString);
            }

            @Test
            @DisplayName("email이 없으면 422")
            void missingEmail() {
                ApiResponse response = client.get("/user/check-email").send().assertStatus(422);
                assertEquals("email", response.json("/detail/0/loc/1").asText(), response::toString);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("있는 이메일은 false, 없는 이메일은 true")
            void checksExistence() {
                AppUser user = createUser();
                UserService service = bean(UserService.class);

                assertFalse(service.isEmailAvailable(user.getEmail()).isAvailable());
                assertTrue(service.isEmailAvailable(unique("free") + "@test.com").isAvailable());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/user/check-nickname")
    @DisplayName("GET /user/check-nickname - 닉네임 중복 확인")
    class CheckNickname {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("아무도 안 쓰는 닉네임이면 available=true")
            void available() {
                ApiResponse response = client.get("/user/check-nickname").query("nickname", unique("free"))
                        .send().assertStatus(200);
                assertTrue(response.json("/available").asBoolean(false), response::toString);
            }

            @Test
            @DisplayName("이미 있는 닉네임이면 available=false")
            void taken() {
                AppUser user = createUser();

                ApiResponse response = client.get("/user/check-nickname").query("nickname", user.getNickname())
                        .send().assertStatus(200);
                assertFalse(response.json("/available").asBoolean(true), response::toString);
            }

            @Test
            @DisplayName("nickname이 없으면 422")
            void missingNickname() {
                ApiResponse response = client.get("/user/check-nickname").send().assertStatus(422);
                assertEquals("nickname", response.json("/detail/0/loc/1").asText(), response::toString);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("있는 닉네임은 false, 없는 닉네임은 true")
            void checksExistence() {
                AppUser user = createUser();
                UserService service = bean(UserService.class);

                assertFalse(service.isNicknameAvailable(user.getNickname()).isAvailable());
                assertTrue(service.isNicknameAvailable(unique("free")).isAvailable());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/user/me")
    @DisplayName("GET /user/me - 내 정보 조회")
    class GetMe {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 내 정보를 명세 필드 이름(camelCase)으로 돌려준다")
            void success() {
                AppUser user = createFullUser();

                ApiResponse response = client.get("/user/me").bearer(login(user)).send().assertStatus(200);

                assertEquals(user.getId(), response.json("/userId").asLong());
                assertEquals(user.getName(), response.json("/name").asText());
                assertEquals(user.getNickname(), response.json("/nickname").asText());
                assertEquals(user.getEmail(), response.json("/email").asText());
                assertEquals(user.getPostal(), response.json("/postalCode").asText());
                assertEquals(user.getAddress(), response.json("/address").asText());
                assertEquals(user.getPhone(), response.json("/phone").asText());
                assertFalse(response.json("/createdAt").asText().isBlank(), response::toString);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/user/me").send().assertStatus(401);
            }

            @Test
            @DisplayName("JWT가 아닌 토큰이면 401")
            void invalidToken() {
                client.get("/user/me").bearer("not-a-jwt").send().assertStatus(401);
            }

            @Test
            @DisplayName("서명이 바뀐 토큰이면 401")
            void tamperedToken() {
                String mine = login(createUser());
                String others = login(createUser());
                //내 header.payload에 다른 토큰의 서명을 붙인다
                String tampered = mine.substring(0, mine.lastIndexOf('.')) + others.substring(others.lastIndexOf('.'));

                client.get("/user/me").bearer(tampered).send().assertStatus(401);
            }

            @Test
            @DisplayName("[BUG] refresh 토큰을 access 토큰 자리에 쓰면 401")
            void refreshTokenRejected() {
                //BUG: CurrentUser.authenticate -> JwtTokenProvider.validateToken이 type=refresh 토큰도 통과시켜 refresh 토큰으로 API를 부를 수 있다
                AppUser user = createUser();
                String refreshToken = bean(JwtTokenProvider.class).createRefreshToken(user.getId());

                client.get("/user/me").bearer(refreshToken).send().assertStatus(401);
            }

            @Test
            @DisplayName("토큰의 유저가 사라졌으면 404")
            void userDeleted() {
                AppUser user = createUser();
                String token = login(user);
                bean(UserRepository.class).delete(user.getId());

                client.get("/user/me").bearer(token).send().assertStatus(404);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("유저 필드를 MyInfoResponse로 옮긴다 (postal -> postalCode)")
            void mapsFields() {
                AppUser user = createFullUser();

                MyInfoResponse result = bean(UserService.class).getUser(user.getId());

                assertEquals(user.getId(), result.getUserId());
                assertEquals(user.getName(), result.getName());
                assertEquals(user.getNickname(), result.getNickname());
                assertEquals(user.getEmail(), result.getEmail());
                assertEquals(user.getPostal(), result.getPostalCode());
                assertEquals(user.getAddress(), result.getAddress());
                assertEquals(user.getPhone(), result.getPhone());
                assertNotNull(result.getCreatedAt());
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                assertThrows(NotFoundException.class, () -> bean(UserService.class).getUser(user.getId()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/user/me")
    @DisplayName("PATCH /user/me - 내 정보 수정")
    class UpdateMe {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 내 id를 돌려주고, 보낸 값만 바뀐다")
            void success() {
                AppUser user = createFullUser();
                String token = login(user);
                String newNickname = unique("new");

                ApiResponse response = client.patch("/user/me").bearer(token)
                        .json(Map.of("nickname", newNickname, "postalCode", "99999"))
                        .send().assertStatus(200);
                assertEquals(user.getId(), response.json("/id").asLong(), response::toString);

                JsonNode me = client.get("/user/me").bearer(token).send().assertStatus(200).json();
                assertEquals(newNickname, me.path("nickname").asText());
                assertEquals("99999", me.path("postalCode").asText());
                //안 보낸 값은 그대로
                assertEquals(user.getName(), me.path("name").asText());
                assertEquals(user.getAddress(), me.path("address").asText());
                assertEquals(user.getPhone(), me.path("phone").asText());
                assertEquals(user.getEmail(), me.path("email").asText());
            }

            @Test
            @DisplayName("지금 내 닉네임을 그대로 보내도 200")
            void sameNickname() {
                AppUser user = createUser();

                client.patch("/user/me").bearer(login(user))
                        .json(Map.of("nickname", user.getNickname()))
                        .send().assertStatus(200);
            }

            @Test
            @DisplayName("다른 유저가 쓰는 닉네임이면 409이고 바뀌지 않는다")
            void nicknameTaken() {
                AppUser user = createUser();
                AppUser other = createUser();

                client.patch("/user/me").bearer(login(user))
                        .json(Map.of("nickname", other.getNickname()))
                        .send().assertStatus(409);

                assertEquals(user.getNickname(), reload(user).getNickname());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.patch("/user/me").json(Map.of("name", "x")).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.patch("/user/me").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("필드 타입이 틀리면 422")
            void wrongFieldType() {
                client.patch("/user/me").bearer(login(createUser()))
                        .rawJson("{\"nickname\": {\"a\": 1}}")
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("보낸 값만 DB에 반영한다 (postalCode -> postal)")
            void updatesOnlySentFields() {
                AppUser user = createFullUser();

                IdResponse result = bean(UserService.class).updateMyInfo(user.getId(),
                        UpdateUserRequest.builder().name("바뀐 이름").postalCode("11111").build());

                assertEquals(user.getId(), result.getId());
                AppUser updated = reload(user);
                assertEquals("바뀐 이름", updated.getName());
                assertEquals("11111", updated.getPostal());
                assertEquals(user.getNickname(), updated.getNickname());
                assertEquals(user.getAddress(), updated.getAddress());
                assertEquals(user.getPhone(), updated.getPhone());
            }

            @Test
            @DisplayName("다른 유저의 닉네임이면 AlreadyExistException")
            void nicknameTaken() {
                AppUser user = createUser();
                AppUser other = createUser();

                assertThrows(AlreadyExistException.class, () -> bean(UserService.class).updateMyInfo(user.getId(),
                        UpdateUserRequest.builder().nickname(other.getNickname()).build()));
                assertEquals(user.getNickname(), reload(user).getNickname());
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                assertThrows(NotFoundException.class, () -> bean(UserService.class).updateMyInfo(user.getId(),
                        UpdateUserRequest.builder().name("x").build()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/user/{user_id}")
    @DisplayName("GET /user/{user_id} - 유저 공개 정보 조회")
    class GetUserById {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 id, 닉네임만 돌려준다 (개인정보 없음)")
            void success() {
                AppUser user = createFullUser();

                ApiResponse response = client.get("/user/" + user.getId()).send().assertStatus(200);

                assertEquals(user.getId(), response.json("/userId").asLong());
                assertEquals(user.getNickname(), response.json("/nickname").asText());
                JsonNode body = response.json();
                for (String privateField : List.of("email", "phone", "address", "postalCode", "name")) {
                    assertFalse(body.has(privateField), () -> privateField + "가 공개 응답에 있다: " + response);
                }
            }

            @Test
            @DisplayName("없는 유저면 404")
            void notFound() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                client.get("/user/" + user.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void notNumber() {
                ApiResponse response = client.get("/user/abc").send().assertStatus(422);
                assertEquals("user_id", response.json("/detail/0/loc/1").asText(), response::toString);
            }

            @Test
            @DisplayName("/user/me는 {user_id}로 잡히지 않는다 (토큰 없으면 422가 아니라 401)")
            void meIsNotUserId() {
                client.get("/user/me").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("id와 닉네임을 돌려준다")
            void mapsFields() {
                AppUser user = createUser();

                UserPublicResponse result = bean(UserService.class).getUserById(user.getId());

                assertEquals(user.getId(), result.getUserId());
                assertEquals(user.getNickname(), result.getNickname());
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                bean(UserRepository.class).delete(user.getId());

                assertThrows(NotFoundException.class, () -> bean(UserService.class).getUserById(user.getId()));
            }
        }
    }
}
