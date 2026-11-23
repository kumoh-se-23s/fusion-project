package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import constant.ProjectState;
import exception.InvalidSearchTypeException;
import exception.NotFoundException;
import exception.NotPendingProjectException;
import exception.OutOfRangeException;
import fixture.AdminProjectFixtures;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.Project;
import model.domain.Reward;
import model.dto.ProjectData;
import model.dto.request.project.AdminProjectDataRequest;
import model.dto.request.project.AdminProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ProjectLikeRepository;
import repository.ProjectRepository;
import service.internal.AdminProjectControlService;
import service.internal.AdminProjectQueryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminProjectRouter (/internal/project)")
class AdminProjectRouterTest extends ApiTest {

    private Project createProject(ProjectState state) {
        return AdminProjectFixtures.project(state);
    }

    private Project reload(Project project) {
        return AdminProjectFixtures.reload(project);
    }

    private static boolean containsId(JsonNode datas, long id) {
        return StreamSupport.stream(datas.spliterator(), false)
                .anyMatch(node -> node.path("id").asLong() == id);
    }

    private static List<Long> ids(DataCollectionResponse<ProjectData> response) {
        return response.getDatas().stream().map(ProjectData::getId).toList();
    }

    /** 생성 요청 body. 날짜는 ISO 문자열로 보낸다. */
    private static Map<String, Object> createBody(Long makerId, Long categoryId, String title,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("startDate", startDate.withNano(0).toString());
        request.put("endDate", endDate.withNano(0).toString());
        request.put("purposeAmount", 50000);
        request.put("title", title);
        request.put("text", "관리자가 만든 프로젝트");
        request.put("state", "PENDING_APPROVAL");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("makerId", makerId);
        body.put("categoryId", categoryId);
        body.put("request", request);
        return body;
    }

    private static AdminProjectRequest createRequest(Long makerId, Long categoryId, String title,
                                                     LocalDateTime startDate, LocalDateTime endDate) {
        return AdminProjectRequest.builder()
                .makerId(makerId)
                .categoryId(categoryId)
                .request(AdminProjectDataRequest.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .purposeAmount(new BigDecimal("50000"))
                        .title(title)
                        .text("관리자가 만든 프로젝트")
                        .state(ProjectState.PENDING_APPROVAL)
                        .build())
                .build();
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/internal/project")
    @DisplayName("POST /internal/project - 관리자 프로젝트 생성")
    class CreateProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("[BUG] 200과 id를 돌려주고 보낸 값으로 저장된다")
            void createsProject() {
                // AdminProjectRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException -> 500 (request 중첩 필드도 Project로 옮겨지지 않는다)
                AppUser maker = AdminProjectFixtures.maker();
                Category category = AdminProjectFixtures.category();
                String title = unique("title");

                ApiResponse response = client.post("/internal/project")
                        .json(createBody(maker.getId(), category.getId(), title,
                                LocalDateTime.now(), LocalDateTime.now().plusDays(10)))
                        .send().assertStatus(200);

                Project saved = bean(ProjectRepository.class).findById(response.json("/id").asLong()).orElseThrow();
                assertEquals(title, saved.getTitle());
                assertEquals(maker.getId(), saved.getMaker().getId());
                assertEquals(category.getId(), saved.getCategory().getId());
                assertEquals(ProjectState.PENDING_APPROVAL, saved.getState());
            }

            @Test
            @DisplayName("없는 메이커면 404")
            void makerNotFound() {
                Category category = AdminProjectFixtures.category();

                client.post("/internal/project")
                        .json(createBody(Long.MAX_VALUE, category.getId(), unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().plusDays(10)))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 카테고리면 404")
            void categoryNotFound() {
                AppUser maker = AdminProjectFixtures.maker();

                client.post("/internal/project")
                        .json(createBody(maker.getId(), Long.MAX_VALUE, unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().plusDays(10)))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 종료일이 시작일보다 앞이면 400")
            void endBeforeStart() {
                // 날짜 검사 전에 Serializer.toDomain(AdminProjectRequest)이 SerializeException -> 500
                AppUser maker = AdminProjectFixtures.maker();
                Category category = AdminProjectFixtures.category();

                client.post("/internal/project")
                        .json(createBody(maker.getId(), category.getId(), unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().minusDays(1)))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/internal/project").send().assertStatus(422);
            }

            @Test
            @DisplayName("makerId가 숫자가 아니면 422")
            void badBodyType() {
                client.post("/internal/project")
                        .rawJson("{\"makerId\": \"abc\", \"categoryId\": 1}")
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("[BUG] 요청 값으로 프로젝트를 만든다")
            void createsProject() {
                // AdminProjectRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException
                AppUser maker = AdminProjectFixtures.maker();
                Category category = AdminProjectFixtures.category();
                String title = unique("title");

                IdResponse result = bean(AdminProjectControlService.class).createProject(createRequest(
                        maker.getId(), category.getId(), title, LocalDateTime.now(), LocalDateTime.now().plusDays(10)));

                Project saved = bean(ProjectRepository.class).findById(result.getId()).orElseThrow();
                assertEquals(title, saved.getTitle());
                assertEquals(maker.getId(), saved.getMaker().getId());
                assertEquals(0, new BigDecimal("50000").compareTo(saved.getPurposeAmount()));
            }

            @Test
            @DisplayName("메이커가 없으면 NotFoundException")
            void makerNotFound() {
                Category category = AdminProjectFixtures.category();

                assertThrows(NotFoundException.class, () -> bean(AdminProjectControlService.class).createProject(
                        createRequest(Long.MAX_VALUE, category.getId(), unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().plusDays(1))));
            }

            @Test
            @DisplayName("카테고리가 없으면 NotFoundException")
            void categoryNotFound() {
                AppUser maker = AdminProjectFixtures.maker();

                assertThrows(NotFoundException.class, () -> bean(AdminProjectControlService.class).createProject(
                        createRequest(maker.getId(), Long.MAX_VALUE, unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().plusDays(1))));
            }

            @Test
            @DisplayName("[BUG] 종료일이 시작일 이전이면 OutOfRangeException이고 저장되지 않는다")
            void endBeforeStart() {
                // 날짜 검사 전에 Serializer.toDomain(AdminProjectRequest)이 SerializeException을 던진다
                AppUser maker = AdminProjectFixtures.maker();
                Category category = AdminProjectFixtures.category();

                assertThrows(OutOfRangeException.class, () -> bean(AdminProjectControlService.class).createProject(
                        createRequest(maker.getId(), category.getId(), unique("title"),
                                LocalDateTime.now(), LocalDateTime.now().minusDays(1))));
                assertTrue(bean(ProjectRepository.class).findByUserId(maker.getId()).isEmpty());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/{project_id}")
    @DisplayName("GET /internal/project/{project_id} - 프로젝트 단건 조회")
    class GetProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 프로젝트 데이터를 돌려준다")
            void returnsProject() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                ApiResponse response = client.get("/internal/project/" + saved.getId()).send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/data/id").asLong());
                assertEquals(saved.getMaker().getId().longValue(), response.json("/data/makerId").asLong());
                assertEquals(saved.getCategory().getId().longValue(), response.json("/data/categoryId").asLong());
                assertEquals(saved.getTitle(), response.json("/data/title").asText());
                assertEquals("PENDING_APPROVAL", response.json("/data/state").asText());
                assertEquals(0, new BigDecimal("10000").compareTo(response.json("/data/purposeAmount").decimalValue()));
            }

            @Test
            @DisplayName("없는 id면 404")
            void notFound() {
                client.get("/internal/project/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/project/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB 값을 DTO로 옮긴다")
            void mapsFields() {
                Project saved = createProject(ProjectState.APPROVED);

                ProjectData data = bean(AdminProjectQueryService.class).getProject(saved.getId()).getData();

                assertEquals(saved.getId(), data.getId());
                assertEquals(saved.getMaker().getId(), data.getMakerId());
                assertEquals(saved.getCategory().getId(), data.getCategoryId());
                assertEquals(ProjectState.APPROVED, data.getState());
                assertEquals(saved.getTitle(), data.getTitle());
            }

            @Test
            @DisplayName("없는 id면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminProjectQueryService.class).getProject(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project")
    @DisplayName("GET /internal/project - 프로젝트 전체 조회")
    class GetAllProjects {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("limit 없이 200과 방금 만든 프로젝트를 포함한 목록을 돌려준다")
            void returnsAll() {
                Project saved = createProject(ProjectState.JUST_USER_INPUT);

                ApiResponse response = client.get("/internal/project").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(containsId(datas, saved.getId()), () -> "방금 만든 프로젝트가 없다: " + response);
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                createProject(ProjectState.JUST_USER_INPUT);
                createProject(ProjectState.JUST_USER_INPUT);

                ApiResponse response = client.get("/internal/project").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/datas").size());
                assertEquals(1, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimit() {
                client.get("/internal/project").query("limit", "abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit이 null이면 DB의 모든 프로젝트를 돌려준다")
            void allRows() {
                createProject(ProjectState.JUST_USER_INPUT);

                DataCollectionResponse<ProjectData> result = bean(AdminProjectQueryService.class).getAllProject(null);

                assertEquals(bean(ProjectRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("limit을 주면 그 수만큼 돌려준다")
            void limited() {
                createProject(ProjectState.JUST_USER_INPUT);
                createProject(ProjectState.JUST_USER_INPUT);

                DataCollectionResponse<ProjectData> result = bean(AdminProjectQueryService.class).getAllProject(2);

                assertEquals(2L, result.getAmount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/pending")
    @DisplayName("GET /internal/project/pending - 승인 대기 프로젝트 조회")
    class GetPendingProjects {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 승인 대기 프로젝트만 돌려준다")
            void returnsPendingOnly() {
                Project pending = createProject(ProjectState.PENDING_APPROVAL);
                Project approved = createProject(ProjectState.APPROVED);

                ApiResponse response = client.get("/internal/project/pending").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(containsId(datas, pending.getId()), response::toString);
                assertFalse(containsId(datas, approved.getId()), response::toString);
                datas.forEach(node -> assertEquals("PENDING_APPROVAL", node.path("state").asText()));
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                createProject(ProjectState.PENDING_APPROVAL);
                createProject(ProjectState.PENDING_APPROVAL);

                ApiResponse response = client.get("/internal/project/pending").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/datas").size());
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimit() {
                client.get("/internal/project/pending").query("limit", "x").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("결과는 모두 PENDING_APPROVAL 상태다")
            void allPending() {
                Project pending = createProject(ProjectState.PENDING_APPROVAL);
                createProject(ProjectState.REJECTED);

                DataCollectionResponse<ProjectData> result = bean(AdminProjectQueryService.class).getPendingProject(null);

                assertTrue(ids(result).contains(pending.getId()));
                assertTrue(result.getDatas().stream().allMatch(d -> d.getState() == ProjectState.PENDING_APPROVAL));
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("limit을 주면 그 수만큼 돌려준다")
            void limited() {
                createProject(ProjectState.PENDING_APPROVAL);
                createProject(ProjectState.PENDING_APPROVAL);

                assertEquals(2L, bean(AdminProjectQueryService.class).getPendingProject(2).getAmount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/project/search")
    @DisplayName("GET /internal/project/search - 조건 검색")
    class SearchProjects {

        /** 한 메이커 아래 달성률이 서로 다른 프로젝트 세 개 (목표 100, 모금 50 / 10 / 90) */
        private List<Project> threeProjects(AppUser maker, Category category) {
            return List.of(
                    AdminProjectFixtures.project(maker, category, ProjectState.IN_PROGRESS,
                            new BigDecimal("100"), new BigDecimal("50")),
                    AdminProjectFixtures.project(maker, category, ProjectState.IN_PROGRESS,
                            new BigDecimal("100"), new BigDecimal("10")),
                    AdminProjectFixtures.project(maker, category, ProjectState.APPROVED,
                            new BigDecimal("100"), new BigDecimal("90")));
        }

        private DataCollectionResponse<ProjectData> searchByMaker(AppUser maker, String searchType) {
            return bean(AdminProjectQueryService.class)
                    .getAllProject(null, maker.getNickname(), null, null, null, searchType);
        }

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("[BUG] search_type 없이 제목으로 검색하면 200과 그 프로젝트를 돌려준다")
            void defaultSearchType() {
                // 기본 정렬 DESC_CREATE_AT이 없는 필드 "createAt"(실제는 createdAt)을 써서 QueryErrorException -> 500
                Project saved = createProject(ProjectState.APPROVED);

                ApiResponse response = client.get("/internal/project/search")
                        .query("title", saved.getTitle())
                        .send().assertStatus(200);

                assertEquals(1, response.json("/amount").asLong());
                assertEquals(saved.getId().longValue(), response.json("/datas/0/id").asLong());
            }

            @Test
            @DisplayName("메이커 닉네임 + 모금액 내림차순으로 200과 정렬된 목록을 돌려준다")
            void byMakerDescDonate() {
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> projects = threeProjects(maker, AdminProjectFixtures.category());

                ApiResponse response = client.get("/internal/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("search_type", "DESC_DONATE_AMOUNT")
                        .send().assertStatus(200);

                assertEquals(3, response.json("/amount").asLong());
                assertEquals(projects.get(2).getId().longValue(), response.json("/datas/0/id").asLong());
                assertEquals(projects.get(0).getId().longValue(), response.json("/datas/1/id").asLong());
                assertEquals(projects.get(1).getId().longValue(), response.json("/datas/2/id").asLong());
            }

            @Test
            @DisplayName("state_names를 여러 번 주면 그 상태들만 돌려준다")
            void byStates() {
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> projects = threeProjects(maker, AdminProjectFixtures.category());

                ApiResponse response = client.get("/internal/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("state_names", "APPROVED")
                        .query("state_names", "REJECTED")
                        .query("search_type", "ASC_DONATE_AMOUNT")
                        .send().assertStatus(200);

                assertEquals(1, response.json("/amount").asLong());
                assertEquals(projects.get(2).getId().longValue(), response.json("/datas/0/id").asLong());
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                AppUser maker = AdminProjectFixtures.maker();
                threeProjects(maker, AdminProjectFixtures.category());

                ApiResponse response = client.get("/internal/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("search_type", "ASC_DONATE_AMOUNT")
                        .query("limit", 2)
                        .send().assertStatus(200);

                assertEquals(2, response.json("/datas").size());
            }

            @Test
            @DisplayName("모르는 search_type이면 400")
            void invalidSearchType() {
                client.get("/internal/project/search")
                        .query("search_type", "WHATEVER")
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("없는 메이커 닉네임이면 404")
            void unknownMaker() {
                client.get("/internal/project/search")
                        .query("maker_name", unique("nobody"))
                        .query("search_type", "ASC_DONATE_AMOUNT")
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 카테고리 이름이면 404")
            void unknownCategory() {
                client.get("/internal/project/search")
                        .query("category_name", unique("nocat"))
                        .query("search_type", "ASC_DONATE_AMOUNT")
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("조건에 맞는 프로젝트가 없으면 404")
            void noMatch() {
                client.get("/internal/project/search")
                        .query("title", unique("no-such-title"))
                        .query("search_type", "ASC_DONATE_AMOUNT")
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimit() {
                client.get("/internal/project/search").query("limit", "x").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("카테고리 이름으로 거른다")
            void byCategory() {
                Category category = AdminProjectFixtures.category();
                Project inCategory = AdminProjectFixtures.project(AdminProjectFixtures.maker(), category, ProjectState.APPROVED);
                createProject(ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = bean(AdminProjectQueryService.class)
                        .getAllProject(null, null, category.getName(), null, null, "ASC_DONATE_AMOUNT");

                assertEquals(List.of(inCategory.getId()), ids(result));
            }

            @Test
            @DisplayName("모금액 오름차순으로 정렬한다")
            void ascDonate() {
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = threeProjects(maker, AdminProjectFixtures.category());

                assertEquals(List.of(p.get(1).getId(), p.get(0).getId(), p.get(2).getId()),
                        ids(searchByMaker(maker, "ASC_DONATE_AMOUNT")));
            }

            @Test
            @DisplayName("[BUG] 달성률 오름차순으로 정렬한다")
            void ascAchievementRate() {
                // sortByAchievementRate가 DESC가 아니면 reversed() 해서 ASC/DESC가 뒤집혀 있다
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = threeProjects(maker, AdminProjectFixtures.category());

                assertEquals(List.of(p.get(1).getId(), p.get(0).getId(), p.get(2).getId()),
                        ids(searchByMaker(maker, "ASC_ACHIEVEMENT_RATE")));
            }

            @Test
            @DisplayName("[BUG] 달성률 내림차순으로 정렬한다")
            void descAchievementRate() {
                // sortByAchievementRate가 DESC일 때만 reversed()를 건너뛰어 오름차순이 된다
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = threeProjects(maker, AdminProjectFixtures.category());

                assertEquals(List.of(p.get(2).getId(), p.get(0).getId(), p.get(1).getId()),
                        ids(searchByMaker(maker, "DESC_ACHIEVEMENT_RATE")));
            }

            @Test
            @DisplayName("서포터 수 오름차순으로 정렬한다")
            void ascSupporterCount() {
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = supporterProjects(maker);

                assertEquals(List.of(p.get(0).getId(), p.get(2).getId(), p.get(1).getId()),
                        ids(searchByMaker(maker, "ASC_SUPPORTER_COUNT")));
            }

            @Test
            @DisplayName("[BUG] 서포터 수 내림차순으로 정렬한다")
            void descSupporterCount() {
                // sortBySupporterCount가 DESC_SUPPORTER_COUNT가 아니라 DESC_ACHIEVEMENT_RATE일 때 reversed() 해서 오름차순이 된다
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = supporterProjects(maker);

                assertEquals(List.of(p.get(1).getId(), p.get(2).getId(), p.get(0).getId()),
                        ids(searchByMaker(maker, "DESC_SUPPORTER_COUNT")));
            }

            @Test
            @DisplayName("모르는 search_type이면 InvalidSearchTypeException")
            void invalidSearchType() {
                assertThrows(InvalidSearchTypeException.class, () -> bean(AdminProjectQueryService.class)
                        .getAllProject(null, null, null, null, null, "NOPE"));
            }

            @Test
            @DisplayName("없는 메이커 닉네임이면 NotFoundException")
            void unknownMaker() {
                assertThrows(NotFoundException.class, () -> bean(AdminProjectQueryService.class)
                        .getAllProject(null, unique("nobody"), null, null, null, "ASC_DONATE_AMOUNT"));
            }

            @Test
            @DisplayName("결과가 비면 NotFoundException")
            void emptyResult() {
                AppUser maker = AdminProjectFixtures.maker();
                threeProjects(maker, AdminProjectFixtures.category());

                assertThrows(NotFoundException.class, () -> bean(AdminProjectQueryService.class)
                        .getAllProject(null, maker.getNickname(), null, List.of("CANCELED"), null, "ASC_DONATE_AMOUNT"));
            }

            @Test
            @DisplayName("[BUG] search_type이 없으면 최신순(DESC_CREATE_AT)으로 돌려준다")
            void defaultOrder() {
                // AdminProjectSearchType.DESC_CREATE_AT의 Order 필드명이 "createAt"이라 QueryErrorException
                AppUser maker = AdminProjectFixtures.maker();
                List<Project> p = threeProjects(maker, AdminProjectFixtures.category());

                DataCollectionResponse<ProjectData> result = searchByMaker(maker, null);

                assertEquals(3L, result.getAmount());
                assertTrue(ids(result).containsAll(p.stream().map(Project::getId).toList()));
            }

            /** 서포터 0 / 2 / 1 명인 프로젝트 세 개 */
            private List<Project> supporterProjects(AppUser maker) {
                Category category = AdminProjectFixtures.category();
                Project none = AdminProjectFixtures.project(maker, category, ProjectState.IN_PROGRESS);
                Project two = AdminProjectFixtures.project(maker, category, ProjectState.IN_PROGRESS);
                Project one = AdminProjectFixtures.project(maker, category, ProjectState.IN_PROGRESS);
                Reward twoReward = AdminProjectFixtures.reward(two);
                AdminProjectFixtures.donate(createUser(), twoReward);
                AdminProjectFixtures.donate(createUser(), twoReward);
                AdminProjectFixtures.donate(createUser(), AdminProjectFixtures.reward(one));
                AdminProjectFixtures.reward(none);
                return List.of(none, two, one);
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/project/{project_id}")
    @DisplayName("PATCH /internal/project/{project_id} - 관리자 프로젝트 수정")
    class UpdateProject {

        private Map<String, Object> editBody(Long makerId, Long categoryId, Map<String, Object> request) {
            Map<String, Object> body = new LinkedHashMap<>();
            if (makerId != null) body.put("makerId", makerId);
            if (categoryId != null) body.put("categoryId", categoryId);
            if (request != null) body.put("request", request);
            return body;
        }

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 id를 돌려주고 메이커, 카테고리, 내용이 바뀐다")
            void updates() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);
                AppUser newMaker = AdminProjectFixtures.maker();
                Category newCategory = AdminProjectFixtures.category();
                String newTitle = unique("title");

                ApiResponse response = client.patch("/internal/project/" + saved.getId())
                        .json(editBody(newMaker.getId(), newCategory.getId(),
                                Map.of("title", newTitle, "text", "바뀐 내용", "state", "APPROVED")))
                        .send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/internal/project/" + saved.getId()).send().assertStatus(200);
                assertEquals(newTitle, after.json("/data/title").asText());
                assertEquals("바뀐 내용", after.json("/data/text").asText());
                assertEquals("APPROVED", after.json("/data/state").asText());
                assertEquals(newMaker.getId().longValue(), after.json("/data/makerId").asLong());
                assertEquals(newCategory.getId().longValue(), after.json("/data/categoryId").asLong());
            }

            @Test
            @DisplayName("[BUG] makerId, categoryId 없이 내용만 보내면 메이커와 카테고리는 그대로다")
            void contentOnly() {
                // editProject가 null인 makerId/categoryId도 dtoMap에 넣어 maker=null -> NotNullException(400), category는 null로 지워진다
                Project saved = createProject(ProjectState.PENDING_APPROVAL);
                String newTitle = unique("title");

                client.patch("/internal/project/" + saved.getId())
                        .json(editBody(null, null, Map.of("title", newTitle)))
                        .send().assertStatus(200);

                Project after = reload(saved);
                assertEquals(newTitle, after.getTitle());
                assertEquals(saved.getMaker().getId(), after.getMaker().getId());
                assertNotNull(after.getCategory());
                assertEquals(saved.getCategory().getId(), after.getCategory().getId());
            }

            @Test
            @DisplayName("[BUG] request 없이 메이커만 보내면 메이커만 바뀐다")
            void makerOnly() {
                // editProject가 request.getRequest()를 null 검사 없이 toUpdateMap() 호출 -> NPE -> 500
                Project saved = createProject(ProjectState.PENDING_APPROVAL);
                AppUser newMaker = AdminProjectFixtures.maker();

                client.patch("/internal/project/" + saved.getId())
                        .json(editBody(newMaker.getId(), saved.getCategory().getId(), null))
                        .send().assertStatus(200);

                Project after = reload(saved);
                assertEquals(newMaker.getId(), after.getMaker().getId());
                assertEquals(saved.getTitle(), after.getTitle());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                Project other = createProject(ProjectState.PENDING_APPROVAL);

                client.patch("/internal/project/" + Long.MAX_VALUE)
                        .json(editBody(other.getMaker().getId(), other.getCategory().getId(), Map.of("title", "x")))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 카테고리 id면 404이고 바뀌지 않는다")
            void categoryNotFound() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                client.patch("/internal/project/" + saved.getId())
                        .json(editBody(saved.getMaker().getId(), Long.MAX_VALUE, Map.of("title", unique("t"))))
                        .send().assertStatus(404);

                assertEquals(saved.getTitle(), reload(saved).getTitle());
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);
                client.patch("/internal/project/" + saved.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/project/abc")
                        .json(editBody(1L, 1L, Map.of("title", "x")))
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("보낸 값으로 DB가 바뀐다")
            void updatesRow() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);
                AppUser newMaker = AdminProjectFixtures.maker();
                String newTitle = unique("title");

                IdResponse result = bean(AdminProjectControlService.class).editProject(saved.getId(),
                        AdminProjectRequest.builder()
                                .makerId(newMaker.getId())
                                .categoryId(saved.getCategory().getId())
                                .request(AdminProjectDataRequest.builder()
                                        .title(newTitle)
                                        .donateAmount(new BigDecimal("777"))
                                        .build())
                                .build());

                assertEquals(saved.getId(), result.getId());
                Project after = reload(saved);
                assertEquals(newTitle, after.getTitle());
                assertEquals(newMaker.getId(), after.getMaker().getId());
                assertEquals(0, new BigDecimal("777").compareTo(after.getDonateAmount()));
                assertEquals(saved.getText(), after.getText());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void notFound() {
                Project other = createProject(ProjectState.PENDING_APPROVAL);

                assertThrows(NotFoundException.class, () -> bean(AdminProjectControlService.class).editProject(
                        Long.MAX_VALUE, AdminProjectRequest.builder()
                                .makerId(other.getMaker().getId())
                                .categoryId(other.getCategory().getId())
                                .request(AdminProjectDataRequest.builder().title("x").build())
                                .build()));
            }

            @Test
            @DisplayName("없는 메이커 id면 NotFoundException")
            void makerNotFound() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                assertThrows(NotFoundException.class, () -> bean(AdminProjectControlService.class).editProject(
                        saved.getId(), AdminProjectRequest.builder()
                                .makerId(Long.MAX_VALUE)
                                .categoryId(saved.getCategory().getId())
                                .request(AdminProjectDataRequest.builder().title("x").build())
                                .build()));
                assertEquals(saved.getMaker().getId(), reload(saved).getMaker().getId());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/project/approve/{project_id}")
    @DisplayName("PATCH /internal/project/approve/{project_id} - 프로젝트 승인")
    class ApproveProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("승인 대기 프로젝트면 200과 id를 돌려주고 APPROVED가 된다")
            void approves() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                ApiResponse response = client.patch("/internal/project/approve/" + saved.getId()).send().assertStatus(200);

                assertEquals(saved.getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/internal/project/" + saved.getId()).send().assertStatus(200);
                assertEquals("APPROVED", after.json("/data/state").asText());
            }

            @Test
            @DisplayName("승인 대기가 아니면 409")
            void notPending() {
                Project saved = createProject(ProjectState.JUST_USER_INPUT);

                client.patch("/internal/project/approve/" + saved.getId()).send().assertStatus(409);
                assertEquals(ProjectState.JUST_USER_INPUT, reload(saved).getState());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                client.patch("/internal/project/approve/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/project/approve/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("PENDING_APPROVAL -> APPROVED로 바꾼다")
            void approves() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                IdResponse result = bean(AdminProjectControlService.class).updateProjectApprove(saved.getId());

                assertEquals(saved.getId(), result.getId());
                assertEquals(ProjectState.APPROVED, reload(saved).getState());
            }

            @Test
            @DisplayName("이미 승인된 프로젝트면 NotPendingProjectException")
            void alreadyApproved() {
                Project saved = createProject(ProjectState.APPROVED);

                assertThrows(NotPendingProjectException.class,
                        () -> bean(AdminProjectControlService.class).updateProjectApprove(saved.getId()));
            }

            @Test
            @DisplayName("거절된 프로젝트면 NotPendingProjectException이고 상태가 그대로다")
            void rejected() {
                Project saved = createProject(ProjectState.REJECTED);

                assertThrows(NotPendingProjectException.class,
                        () -> bean(AdminProjectControlService.class).updateProjectApprove(saved.getId()));
                assertEquals(ProjectState.REJECTED, reload(saved).getState());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(AdminProjectControlService.class).updateProjectApprove(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/project/{project_id}")
    @DisplayName("DELETE /internal/project/{project_id} - 프로젝트 삭제")
    class DeleteProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                ApiResponse response = client.delete("/internal/project/" + saved.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/project/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 프로젝트면 200과 result=false")
            void notFound() {
                ApiResponse response = client.delete("/internal/project/" + Long.MAX_VALUE).send().assertStatus(200);

                assertFalse(response.json("/result").asBoolean(), response::toString);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/project/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB에서 행이 사라진다")
            void removesRow() {
                Project saved = createProject(ProjectState.PENDING_APPROVAL);

                assertTrue(bean(AdminProjectControlService.class).deleteProject(saved.getId()).getResult());
                assertTrue(bean(ProjectRepository.class).findById(saved.getId()).isEmpty());
            }

            @Test
            @DisplayName("없는 프로젝트면 result=false")
            void notFound() {
                assertFalse(bean(AdminProjectControlService.class).deleteProject(Long.MAX_VALUE).getResult());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/project/like/{project_id}")
    @DisplayName("DELETE /internal/project/like/{project_id} - 프로젝트 좋아요 전체 삭제")
    class DeleteProjectLikes {

        /** 좋아요 likes개, 싫어요 dislikes개가 달리고 카운트도 맞춰진 프로젝트 */
        private Project projectWithLikes(int likes, int dislikes) {
            Project project = createProject(ProjectState.IN_PROGRESS);
            for (int i = 0; i < likes; i++) {
                AdminProjectFixtures.like(project, createUser(), true);
            }
            for (int i = 0; i < dislikes; i++) {
                AdminProjectFixtures.like(project, createUser(), false);
            }
            ProjectRepository repository = bean(ProjectRepository.class);
            repository.updateByLike(project.getId(), (long) likes).orElseThrow();
            repository.updateByDislike(project.getId(), (long) dislikes).orElseThrow();
            return reload(project);
        }

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 좋아요/싫어요가 모두 지워진다")
            void deletesAll() {
                Project project = projectWithLikes(1, 1);

                ApiResponse response = client.delete("/internal/project/like/" + project.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                assertTrue(bean(ProjectLikeRepository.class).findByProject(project.getId()).isEmpty());
                ApiResponse after = client.get("/internal/project/" + project.getId()).send().assertStatus(200);
                assertEquals(0, after.json("/data/likeCount").asLong());
                assertEquals(0, after.json("/data/dislikeCount").asLong());
            }

            @Test
            @DisplayName("좋아요가 없는 프로젝트도 200과 result=true")
            void noLikes() {
                Project project = createProject(ProjectState.IN_PROGRESS);

                ApiResponse response = client.delete("/internal/project/like/" + project.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
            }

            @Test
            @DisplayName("id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/project/like/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("[BUG] 좋아요가 여러 개여도 카운트가 0이 된다")
            void manyLikesResetToZero() {
                // updateLikeValue가 한 번 조회한 같은 Project 인스턴스의 낡은 likeCount에서 1씩 빼서, 2개면 2-1=1로 끝난다
                Project project = projectWithLikes(2, 2);

                assertTrue(bean(AdminProjectControlService.class).deleteProjectAllLikes(project.getId()).getResult());

                Project after = reload(project);
                assertTrue(bean(ProjectLikeRepository.class).findByProject(project.getId()).isEmpty());
                assertEquals(0L, after.getLikeCount());
                assertEquals(0L, after.getDislikeCount());
            }

            @Test
            @DisplayName("다른 프로젝트의 좋아요는 건드리지 않는다")
            void otherProjectUntouched() {
                Project target = projectWithLikes(1, 0);
                Project other = projectWithLikes(1, 0);

                bean(AdminProjectControlService.class).deleteProjectAllLikes(target.getId());

                assertEquals(1, bean(ProjectLikeRepository.class).findByProject(other.getId()).size());
                assertEquals(1L, reload(other).getLikeCount());
            }
        }
    }
}
