package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import constant.ProjectState;
import exception.NotFoundException;
import exception.NotNullException;
import fixture.AdminProjectFixtures;
import model.domain.DenyProject;
import model.domain.Project;
import model.dto.DenyProjectData;
import model.dto.request.deny_project.DenyProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.DenyProjectRepository;
import repository.ProjectRepository;
import service.internal.AdminDenyProjectControlService;
import service.internal.AdminDenyProjectQueryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminDenyProjectRouter (/internal/project/deny)")
class AdminDenyProjectRouterTest extends ApiTest {

    private Project createProject(ProjectState state) {
        return AdminProjectFixtures.project(state);
    }

    private DenyProject createDeny(Project project) {
        return AdminProjectFixtures.denyProject(project, unique("reason"));
    }

    private DenyProject createDeny() {
        return createDeny(createProject(ProjectState.REJECTED));
    }

    private ProjectState stateOf(Project project) {
        return AdminProjectFixtures.reload(project).getState();
    }

    private DenyProject reloadDeny(long id) {
        return bean(DenyProjectRepository.class).findById(id).orElseThrow();
    }

    private static boolean containsId(JsonNode datas, long id) {
        return StreamSupport.stream(datas.spliterator(), false)
                .anyMatch(node -> node.path("id").asLong() == id);
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/internal/project/deny/{project_id}")
    @DisplayName("POST /internal/project/deny/{project_id} - 프로젝트 거절")
    class CreateDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 거절 사유 id를 돌려주고 프로젝트는 REJECTED가 된다")
            void denies() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);
                String reason = unique("reason");

                ApiResponse response = client.post("/internal/project/deny/" + project.getId())
                        .json(Map.of("denyReason", reason))
                        .send().assertStatus(200);

                DenyProject saved = reloadDeny(response.json("/id").asLong());
                assertEquals(reason, saved.getReason());
                assertEquals(project.getId(), saved.getProject().getId());
                ApiResponse after = client.get("/internal/project/" + project.getId()).send().assertStatus(200);
                assertEquals("REJECTED", after.json("/data/state").asText());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void projectNotFound() {
                client.post("/internal/project/deny/" + Long.MAX_VALUE)
                        .json(Map.of("denyReason", "사유"))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("사유가 없으면 400이고 프로젝트 상태는 그대로다")
            void missingReason() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);

                client.post("/internal/project/deny/" + project.getId())
                        .json(Map.of())
                        .send().assertStatus(400);

                assertEquals(ProjectState.PENDING_APPROVAL, stateOf(project));
                assertTrue(bean(DenyProjectRepository.class).findByProject(project.getId()).isEmpty());
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);
                client.post("/internal/project/deny/" + project.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.post("/internal/project/deny/abc")
                        .json(Map.of("denyReason", "사유"))
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("거절 사유를 저장하고 프로젝트를 REJECTED로 바꾼다")
            void denies() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);

                IdResponse result = bean(AdminDenyProjectControlService.class).createDenyProject(project.getId(),
                        DenyProjectRequest.builder().denyReason("부적절한 내용").build());

                assertEquals("부적절한 내용", reloadDeny(result.getId()).getReason());
                assertEquals(ProjectState.REJECTED, stateOf(project));
            }

            @Test
            @DisplayName("같은 프로젝트를 다시 거절하면 사유가 하나 더 쌓인다")
            void denyTwice() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);
                AdminDenyProjectControlService service = bean(AdminDenyProjectControlService.class);

                service.createDenyProject(project.getId(), DenyProjectRequest.builder().denyReason("첫 번째").build());
                service.createDenyProject(project.getId(), DenyProjectRequest.builder().denyReason("두 번째").build());

                assertEquals(2, bean(DenyProjectRepository.class).findByProject(project.getId()).size());
                assertEquals(ProjectState.REJECTED, stateOf(project));
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void projectNotFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminDenyProjectControlService.class)
                        .createDenyProject(Long.MAX_VALUE, DenyProjectRequest.builder().denyReason("사유").build()));
            }

            @Test
            @DisplayName("사유가 null이면 NotNullException이고 상태 변경도 롤백된다")
            void nullReasonRollsBack() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);

                assertThrows(NotNullException.class, () -> bean(AdminDenyProjectControlService.class)
                        .createDenyProject(project.getId(), DenyProjectRequest.builder().build()));
                assertEquals(ProjectState.PENDING_APPROVAL, stateOf(project));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/deny/{deny_project_id}")
    @DisplayName("GET /internal/project/deny/{deny_project_id} - 거절 사유 단건 조회")
    class GetDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 거절 사유 데이터를 돌려준다")
            void returnsDeny() {
                DenyProject saved = createDeny();

                ApiResponse response = client.get("/internal/project/deny/" + saved.getId()).send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/data/id").asLong());
                assertEquals(saved.getProject().getId().longValue(), response.json("/data/projectId").asLong());
                assertEquals(saved.getReason(), response.json("/data/denyReason").asText());
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.get("/internal/project/deny/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/project/deny/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB 값을 DTO로 옮긴다")
            void mapsFields() {
                DenyProject saved = createDeny();

                DenyProjectData data = bean(AdminDenyProjectQueryService.class).getDenyProject(saved.getId()).getData();

                assertEquals(saved.getId(), data.getId());
                assertEquals(saved.getProject().getId(), data.getProjectId());
                assertEquals(saved.getReason(), data.getDenyReason());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminDenyProjectQueryService.class).getDenyProject(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/deny")
    @DisplayName("GET /internal/project/deny - 거절 사유 전체 조회")
    class GetAllDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("limit만큼 200과 목록을 돌려준다")
            void returnsLimited() {
                createDeny();
                createDeny();

                ApiResponse response = client.get("/internal/project/deny").query("limit", 2).send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(2, datas.size());
                assertEquals(2, response.json("/amount").asLong());
                datas.forEach(node -> {
                    assertTrue(node.path("id").asLong() > 0);
                    assertTrue(node.path("projectId").asLong() > 0);
                    assertTrue(node.hasNonNull("denyReason"));
                });
            }

            @Test
            @DisplayName("limit을 크게 주면 방금 만든 사유가 포함된다")
            void containsCreated() {
                DenyProject saved = createDeny();

                ApiResponse response = client.get("/internal/project/deny")
                        .query("limit", Integer.MAX_VALUE).send().assertStatus(200);

                assertTrue(containsId(response.json("/datas"), saved.getId()), response::toString);
            }

            @Test
            @DisplayName("limit이 없으면 422")
            void missingLimit() {
                client.get("/internal/project/deny").send().assertStatus(422);
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimit() {
                client.get("/internal/project/deny").query("limit", "abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit 수만큼 돌려주고 amount가 같다")
            void limited() {
                createDeny();
                createDeny();

                DataCollectionResponse<DenyProjectData> result = bean(AdminDenyProjectQueryService.class).getAllDenyProject(2);

                assertEquals(2L, result.getAmount());
                assertEquals(2, result.getDatas().size());
            }

            @Test
            @DisplayName("행보다 큰 limit이면 DB의 모든 거절 사유를 돌려준다")
            void allRows() {
                createDeny();

                DataCollectionResponse<DenyProjectData> result =
                        bean(AdminDenyProjectQueryService.class).getAllDenyProject(Integer.MAX_VALUE);

                assertEquals(bean(DenyProjectRepository.class).findAll().size(), result.getAmount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/deny/reason/{project_id}")
    @DisplayName("GET /internal/project/deny/reason/{project_id} - 프로젝트별 거절 사유 조회")
    class GetDenyProjectByProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 그 프로젝트의 거절 사유만 돌려준다")
            void returnsByProject() {
                Project project = createProject(ProjectState.REJECTED);
                DenyProject first = createDeny(project);
                DenyProject second = createDeny(project);
                DenyProject other = createDeny();

                ApiResponse response = client.get("/internal/project/deny/reason/" + project.getId())
                        .send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(2, response.json("/amount").asLong());
                assertTrue(containsId(datas, first.getId()));
                assertTrue(containsId(datas, second.getId()));
                assertFalse(containsId(datas, other.getId()));
                datas.forEach(node -> assertEquals(project.getId().longValue(), node.path("projectId").asLong()));
            }

            @Test
            @DisplayName("거절 사유가 없는 프로젝트면 200과 빈 목록")
            void empty() {
                Project project = createProject(ProjectState.PENDING_APPROVAL);

                ApiResponse response = client.get("/internal/project/deny/reason/" + project.getId())
                        .send().assertStatus(200);

                assertEquals(0, response.json("/amount").asLong());
                assertEquals(0, response.json("/datas").size());
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/project/deny/reason/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("프로젝트에 달린 사유 수와 amount가 같다")
            void amountMatches() {
                Project project = createProject(ProjectState.REJECTED);
                createDeny(project);
                createDeny(project);
                createDeny(project);

                DataCollectionResponse<DenyProjectData> result =
                        bean(AdminDenyProjectQueryService.class).getDenyProjectByProject(project.getId());

                assertEquals(3L, result.getAmount());
                assertTrue(result.getDatas().stream().allMatch(d -> d.getProjectId().equals(project.getId())));
            }

            @Test
            @DisplayName("없는 프로젝트면 빈 목록")
            void unknownProject() {
                assertEquals(0L,
                        bean(AdminDenyProjectQueryService.class).getDenyProjectByProject(Long.MAX_VALUE).getAmount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/project/deny/{deny_project_id}")
    @DisplayName("PATCH /internal/project/deny/{deny_project_id} - 거절 사유 수정")
    class UpdateDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("[BUG] 200과 id를 돌려주고 사유가 바뀐다")
            void updates() {
                // toUpdateMap()이 DTO 필드명 "denyReason"을 키로 써서 DenyProject에 없는 필드 -> QueryErrorException -> 500
                DenyProject saved = createDeny();
                String newReason = unique("new-reason");

                ApiResponse response = client.patch("/internal/project/deny/" + saved.getId())
                        .json(Map.of("denyReason", newReason))
                        .send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/internal/project/deny/" + saved.getId()).send().assertStatus(200);
                assertEquals(newReason, after.json("/data/denyReason").asText());
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.patch("/internal/project/deny/" + Long.MAX_VALUE)
                        .json(Map.of("denyReason", "사유"))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                DenyProject saved = createDeny();
                client.patch("/internal/project/deny/" + saved.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/project/deny/abc")
                        .json(Map.of("denyReason", "사유"))
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("[BUG] 사유를 바꾸고 프로젝트를 다시 REJECTED로 돌린다")
            void updatesAndRejects() {
                // toUpdateMap()이 @DataField(name="reason")을 무시하고 "denyReason" 키를 만들어 QueryErrorException
                Project project = createProject(ProjectState.REJECTED);
                DenyProject saved = createDeny(project);
                // 메이커가 다시 승인 요청을 넣은 상태
                bean(ProjectRepository.class).updateByPending(project.getId()).orElseThrow();

                IdResponse result = bean(AdminDenyProjectControlService.class).editDenyProject(saved.getId(),
                        DenyProjectRequest.builder().denyReason("다시 거절").build());

                assertEquals(saved.getId(), result.getId());
                assertEquals("다시 거절", reloadDeny(saved.getId()).getReason());
                assertEquals(ProjectState.REJECTED, stateOf(project));
            }

            @Test
            @DisplayName("사유를 보내지 않으면 사유는 그대로다")
            void nullReasonKeeps() {
                DenyProject saved = createDeny();

                bean(AdminDenyProjectControlService.class).editDenyProject(saved.getId(),
                        DenyProjectRequest.builder().build());

                assertEquals(saved.getReason(), reloadDeny(saved.getId()).getReason());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminDenyProjectControlService.class)
                        .editDenyProject(Long.MAX_VALUE, DenyProjectRequest.builder().denyReason("x").build()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/project/deny/{deny_project_id}")
    @DisplayName("DELETE /internal/project/deny/{deny_project_id} - 거절 사유 삭제")
    class DeleteDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                DenyProject saved = createDeny();

                ApiResponse response = client.delete("/internal/project/deny/" + saved.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/project/deny/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.delete("/internal/project/deny/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/project/deny/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("거절 사유만 지우고 프로젝트는 남는다")
            void removesOnlyDeny() {
                Project project = createProject(ProjectState.REJECTED);
                DenyProject saved = createDeny(project);

                assertTrue(bean(AdminDenyProjectControlService.class).deleteDenyProject(saved.getId()).getResult());

                assertTrue(bean(DenyProjectRepository.class).findById(saved.getId()).isEmpty());
                assertTrue(bean(ProjectRepository.class).findById(project.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminDenyProjectControlService.class).deleteDenyProject(Long.MAX_VALUE));
            }
        }
    }
}
