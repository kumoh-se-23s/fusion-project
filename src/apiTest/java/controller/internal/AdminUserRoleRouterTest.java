package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import constant.Role;
import exception.AlreadyExistException;
import exception.NotFoundException;
import fixture.AdminUserFixtures;
import model.domain.AppUser;
import model.domain.UserRoleRelation;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.UserRoleRelationRepository;
import repository.UserRoleRepository;
import service.internal.AdminUserRoleService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static fixture.AdminUserFixtures.MISSING_ID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminUserRoleRouter (/internal/user/{user_id}/role)")
class AdminUserRoleRouterTest extends ApiTest {

    private static Set<String> roleNames(JsonNode array) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toSet());
    }

    private List<Role> rolesInDb(long userId) {
        return bean(UserRoleRelationRepository.class).findByUserId(userId).stream()
                .map(r -> r.getRole().getRole())
                .toList();
    }

    /**
     * 권한 마스터 행을 잠깐 지우고 작업을 실행한 뒤 다시 만든다.
     * (권한 마스터를 지우면 그 권한의 유저-권한 연결도 같이 지워진다)
     */
    private void withoutRoleMaster(Role role, Runnable action) {
        UserRoleRepository repository = bean(UserRoleRepository.class);
        repository.findByRole(role).ifPresent(r -> repository.delete(r.getId()));
        try {
            action.run();
        } finally {
            AdminUserFixtures.role(role);
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/user/{user_id}/role")
    @DisplayName("GET /internal/user/{user_id}/role - 유저 권한 조회")
    class GetRoleByUser {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 유저가 가진 권한 이름 목록을 돌려준다")
            void returnsRoles() {
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);
                AdminUserFixtures.grant(user, Role.MAKER);

                ApiResponse response = client.get("/internal/user/" + user.getId() + "/role").send().assertStatus(200);

                JsonNode roles = response.json("/userRole");
                assertTrue(roles.isArray(), response::toString);
                assertEquals(Set.of("USER", "MAKER"), roleNames(roles));
            }

            @Test
            @DisplayName("권한이 없는 유저는 빈 목록")
            void emptyRoles() {
                AppUser user = createUser();

                ApiResponse response = client.get("/internal/user/" + user.getId() + "/role").send().assertStatus(200);

                assertTrue(response.json("/userRole").isArray(), response::toString);
                assertEquals(0, response.json("/userRole").size());
            }

            @Test
            @DisplayName("없는 유저면 404")
            void notFound() {
                client.get("/internal/user/" + MISSING_ID + "/role").send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/user/abc/role").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("다른 유저의 권한은 섞이지 않는다")
            void onlyOwnRoles() {
                AppUser user = createUser();
                AppUser other = createUser();
                AdminUserFixtures.grant(user, Role.ADMIN);
                AdminUserFixtures.grant(other, Role.MAKER);

                List<Role> roles = bean(AdminUserRoleService.class).getRoleByUser(user.getId()).getUserRole();

                assertEquals(List.of(Role.ADMIN), roles);
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminUserRoleService.class).getRoleByUser(MISSING_ID));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/internal/user/{user_id}/role/{role}")
    @DisplayName("POST /internal/user/{user_id}/role/{role} - 유저 권한 부여")
    class AssignRole {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 유저-권한 연결 id를 돌려주고 조회에 보인다")
            void assigns() {
                AdminUserFixtures.role(Role.MAKER);
                AppUser user = createUser();

                ApiResponse response = client.post("/internal/user/" + user.getId() + "/role/MAKER").send().assertStatus(200);

                long relationId = response.json("/id").asLong();
                UserRoleRelation relation = bean(UserRoleRelationRepository.class).findById(relationId).orElseThrow();
                assertEquals(user.getId(), relation.getUser().getId());
                assertEquals(Role.MAKER, relation.getRole().getRole());

                ApiResponse roles = client.get("/internal/user/" + user.getId() + "/role").send().assertStatus(200);
                assertEquals(Set.of("MAKER"), roleNames(roles.json("/userRole")));
            }

            @Test
            @DisplayName("모든 Role 이름을 받는다")
            void everyRole() {
                AppUser user = createUser();
                for (Role role : Role.values()) {
                    AdminUserFixtures.role(role);
                    client.post("/internal/user/" + user.getId() + "/role/" + role.name()).send().assertStatus(200);
                }

                assertEquals(Set.of(Role.values()), Set.copyOf(rolesInDb(user.getId())));
            }

            @Test
            @DisplayName("소문자 권한 이름도 받는다 (enum 이름 대소문자 무시)")
            void lowercaseRole() {
                AdminUserFixtures.role(Role.ADMIN);
                AppUser user = createUser();

                client.post("/internal/user/" + user.getId() + "/role/admin").send().assertStatus(200);

                assertEquals(List.of(Role.ADMIN), rolesInDb(user.getId()));
            }

            @Test
            @DisplayName("없는 권한 이름이면 422")
            void invalidRole() {
                AppUser user = createUser();

                ApiResponse response = client.post("/internal/user/" + user.getId() + "/role/SUPERUSER").send().assertStatus(422);

                assertTrue(response.body().contains("enum"), response::toString);
                assertTrue(rolesInDb(user.getId()).isEmpty());
            }

            @Test
            @DisplayName("이미 가진 권한이면 409")
            void duplicate() {
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);

                client.post("/internal/user/" + user.getId() + "/role/USER").send().assertStatus(409);

                assertEquals(List.of(Role.USER), rolesInDb(user.getId()));
            }

            @Test
            @DisplayName("없는 유저면 404")
            void userNotFound() {
                AdminUserFixtures.role(Role.USER);

                client.post("/internal/user/" + MISSING_ID + "/role/USER").send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.post("/internal/user/abc/role/USER").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("연결을 만들고 그 id를 돌려준다")
            void createsRelation() {
                AdminUserFixtures.role(Role.ADMIN);
                AppUser user = createUser();

                IdResponse result = bean(AdminUserRoleService.class).assignRole(user.getId(), Role.ADMIN);

                UserRoleRelation relation = bean(UserRoleRelationRepository.class).findById(result.getId()).orElseThrow();
                assertEquals(user.getId(), relation.getUser().getId());
                assertEquals(Role.ADMIN, relation.getRole().getRole());
            }

            @Test
            @DisplayName("같은 권한을 두 번 주면 AlreadyExistException이고 연결은 하나뿐이다")
            void duplicate() {
                AdminUserFixtures.role(Role.MAKER);
                AppUser user = createUser();
                AdminUserRoleService service = bean(AdminUserRoleService.class);

                service.assignRole(user.getId(), Role.MAKER);
                assertThrows(AlreadyExistException.class, () -> service.assignRole(user.getId(), Role.MAKER));

                assertEquals(List.of(Role.MAKER), rolesInDb(user.getId()));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void userNotFound() {
                AdminUserFixtures.role(Role.USER);

                assertThrows(NotFoundException.class,
                        () -> bean(AdminUserRoleService.class).assignRole(MISSING_ID, Role.USER));
            }

            @Test
            @DisplayName("권한 마스터 데이터가 없으면 NotFoundException")
            void roleMasterMissing() {
                AppUser user = createUser();

                withoutRoleMaster(Role.GUEST, () -> assertThrows(NotFoundException.class,
                        () -> bean(AdminUserRoleService.class).assignRole(user.getId(), Role.GUEST)));

                assertTrue(rolesInDb(user.getId()).isEmpty());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/user/{user_id}/role/{role}")
    @DisplayName("DELETE /internal/user/{user_id}/role/{role} - 유저 권한 회수")
    class RemoveRole {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 그 권한만 빠진다")
            void removes() {
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);
                AdminUserFixtures.grant(user, Role.MAKER);

                ApiResponse response = client.delete("/internal/user/" + user.getId() + "/role/MAKER").send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                ApiResponse roles = client.get("/internal/user/" + user.getId() + "/role").send().assertStatus(200);
                assertEquals(Set.of("USER"), roleNames(roles.json("/userRole")));
            }

            @Test
            @DisplayName("가지지 않은 권한이면 404")
            void notAssigned() {
                AdminUserFixtures.role(Role.ADMIN);
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);

                client.delete("/internal/user/" + user.getId() + "/role/ADMIN").send().assertStatus(404);

                assertEquals(List.of(Role.USER), rolesInDb(user.getId()));
            }

            @Test
            @DisplayName("없는 권한 이름이면 422")
            void invalidRole() {
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);

                client.delete("/internal/user/" + user.getId() + "/role/NOPE").send().assertStatus(422);

                assertEquals(List.of(Role.USER), rolesInDb(user.getId()));
            }

            @Test
            @DisplayName("없는 유저면 404")
            void userNotFound() {
                AdminUserFixtures.role(Role.USER);

                client.delete("/internal/user/" + MISSING_ID + "/role/USER").send().assertStatus(404);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/user/abc/role/USER").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("연결 행을 지우고 권한 마스터와 다른 유저의 연결은 남긴다")
            void removesOnlyRelation() {
                AppUser user = createUser();
                AppUser other = createUser();
                UserRoleRelation relation = AdminUserFixtures.grant(user, Role.MAKER);
                AdminUserFixtures.grant(other, Role.MAKER);

                assertTrue(bean(AdminUserRoleService.class).removeRole(user.getId(), Role.MAKER).getResult());

                assertFalse(bean(UserRoleRelationRepository.class).isExistById(relation.getId()));
                assertTrue(bean(UserRoleRepository.class).findByRole(Role.MAKER).isPresent());
                assertEquals(List.of(Role.MAKER), rolesInDb(other.getId()));
            }

            @Test
            @DisplayName("가지지 않은 권한을 빼면 NotFoundException")
            void notAssigned() {
                AdminUserFixtures.role(Role.GUEST);
                AppUser user = createUser();

                assertThrows(NotFoundException.class,
                        () -> bean(AdminUserRoleService.class).removeRole(user.getId(), Role.GUEST));
            }

            @Test
            @DisplayName("두 번 빼면 두 번째는 NotFoundException")
            void removeTwice() {
                AppUser user = createUser();
                AdminUserFixtures.grant(user, Role.USER);
                AdminUserRoleService service = bean(AdminUserRoleService.class);

                service.removeRole(user.getId(), Role.USER);
                assertThrows(NotFoundException.class, () -> service.removeRole(user.getId(), Role.USER));
            }

            @Test
            @DisplayName("권한 마스터 데이터가 없으면 NotFoundException")
            void roleMasterMissing() {
                AppUser user = createUser();

                withoutRoleMaster(Role.GUEST, () -> assertThrows(NotFoundException.class,
                        () -> bean(AdminUserRoleService.class).removeRole(user.getId(), Role.GUEST)));
            }
        }
    }
}
