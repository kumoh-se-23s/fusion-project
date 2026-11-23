package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import constant.ProjectState;
import exception.InvalidSearchTypeException;
import exception.InvalidStateException;
import exception.InvalidUserException;
import exception.NotFoundException;
import exception.OutOfRangeException;
import fixture.ProjectFixtures;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.DenyProject;
import model.domain.Project;
import model.domain.Reward;
import model.dto.DenyProjectData;
import model.dto.ProjectData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.EditProjectDataRequest;
import model.dto.request.project.EditProjectRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ProjectRepository;
import repository.UserRepository;
import service.external.ProjectMakerService;
import service.external.ProjectQueryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectRouter (/project)")
class ProjectRouterTest extends ApiTest {

    // ===================================================================== 공통 헬퍼

    private Project project(AppUser maker, ProjectState state) {
        return ProjectFixtures.project(maker, state);
    }

    private Project findProject(long projectId) {
        return bean(ProjectRepository.class).findById(projectId).orElseThrow();
    }

    /** 응답 datas 배열에서 id 목록을 순서대로 꺼낸다 */
    private static List<Long> ids(JsonNode datas) {
        return StreamSupport.stream(datas.spliterator(), false)
                .map(node -> node.path("id").asLong())
                .toList();
    }

    private static List<Long> ids(DataCollectionResponse<ProjectData> result) {
        return result.getDatas().stream().map(ProjectData::getId).toList();
    }

    /** 프로젝트 생성 요청 body. 날짜는 ISO 문자열로 보낸다 */
    private static Map<String, Object> createBody(Long categoryId, String title, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("text", title + " 본문");
        body.put("categoryId", categoryId);
        if (start != null) {
            body.put("startDate", start.withNano(0).toString());
        }
        if (end != null) {
            body.put("endDate", end.withNano(0).toString());
        }
        return body;
    }

    private static Map<String, Object> editBody(String title, String text, Long categoryId) {
        Map<String, Object> data = new HashMap<>();
        if (title != null) {
            data.put("title", title);
        }
        if (text != null) {
            data.put("text", text);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("projectDataDTO", data);
        if (categoryId != null) {
            body.put("categoryId", categoryId);
        }
        return body;
    }

    // ===================================================================== POST /project

    @Nested
    @ApiEndpoint(method = "POST", path = "/project")
    @DisplayName("POST /project - 프로젝트 생성")
    class CreateProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인한 유저가 만들면 200과 id를 돌려주고 DB에 JUST_USER_INPUT 상태로 저장된다")
            void createsProject() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                String title = unique("title");
                Map<String, Object> body = createBody(category.getId(), title,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30));
                body.put("imgFile", Base64.getEncoder().encodeToString("img".getBytes()));

                ApiResponse response = client.post("/project").bearer(login(maker)).json(body).send().assertStatus(200);

                long id = response.json("/id").asLong();
                assertTrue(id > 0, response::toString);
                Project saved = findProject(id);
                assertEquals(maker.getId(), saved.getMaker().getId());
                assertEquals(category.getId(), saved.getCategory().getId());
                assertEquals(title, saved.getTitle());
                assertEquals(title + " 본문", saved.getText());
                assertEquals(ProjectState.JUST_USER_INPUT, saved.getState());
                assertEquals(0L, saved.getLikeCount());
                assertEquals(0L, saved.getDislikeCount());
                assertArrayEquals("img".getBytes(), saved.getImgFile());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Category category = ProjectFixtures.category();
                client.post("/project")
                        .json(createBody(category.getId(), unique("title"), null, LocalDateTime.now().plusDays(3)))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                Category category = ProjectFixtures.category();
                client.post("/project").bearer("invalid.token.value")
                        .json(createBody(category.getId(), unique("title"), null, LocalDateTime.now().plusDays(3)))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/project").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("날짜 형식이 틀리면 422")
            void invalidDateFormat() {
                Category category = ProjectFixtures.category();
                Map<String, Object> body = createBody(category.getId(), unique("title"), null, null);
                body.put("endDate", "내일");

                client.post("/project").bearer(login(createUser())).json(body).send().assertStatus(422);
            }

            @Test
            @DisplayName("깨진 JSON이면 422")
            void brokenJson() {
                client.post("/project").bearer(login(createUser())).rawJson("{\"title\": ").send().assertStatus(422);
            }

            @Test
            @DisplayName("종료일이 시작일보다 앞이면 400")
            void endBeforeStart() {
                Category category = ProjectFixtures.category();
                client.post("/project").bearer(login(createUser()))
                        .json(createBody(category.getId(), unique("title"),
                                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(5)))
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("없는 카테고리면 404")
            void unknownCategory() {
                client.post("/project").bearer(login(createUser()))
                        .json(createBody(Long.MAX_VALUE, unique("title"), null, LocalDateTime.now().plusDays(5)))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰 주인이 DB에 없으면 404")
            void deletedUser() {
                AppUser maker = createUser();
                String token = login(maker);
                bean(UserRepository.class).delete(maker.getId());
                Category category = ProjectFixtures.category();

                client.post("/project").bearer(token)
                        .json(createBody(category.getId(), unique("title"), null, LocalDateTime.now().plusDays(5)))
                        .send().assertStatus(404);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            private CreateProjectRequest request(Long categoryId, LocalDateTime start, LocalDateTime end, String title) {
                return CreateProjectRequest.builder()
                        .categoryId(categoryId)
                        .startDate(start)
                        .endDate(end)
                        .title(title)
                        .text("본문")
                        .build();
            }

            @Test
            @DisplayName("메이커와 카테고리를 연결해 저장한다")
            void savesWithMakerAndCategory() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                String title = unique("title");

                IdResponse result = bean(ProjectMakerService.class).createProject(maker.getId(),
                        request(category.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(7), title));

                Project saved = findProject(result.getId());
                assertEquals(maker.getId(), saved.getMaker().getId());
                assertEquals(category.getId(), saved.getCategory().getId());
                assertEquals(title, saved.getTitle());
                assertEquals(ProjectState.JUST_USER_INPUT, saved.getState());
                assertEquals(0, BigDecimal.ZERO.compareTo(saved.getDonateAmount()));
            }

            @Test
            @DisplayName("시작일을 비우면 지금 시각이 시작일이 된다")
            void defaultStartDate() {
                LocalDateTime before = LocalDateTime.now().minusMinutes(1);

                IdResponse result = bean(ProjectMakerService.class).createProject(createUser().getId(),
                        request(ProjectFixtures.category().getId(), null, LocalDateTime.now().plusDays(7), unique("title")));

                LocalDateTime startDate = findProject(result.getId()).getStartDate();
                assertNotNull(startDate);
                assertTrue(startDate.isAfter(before), () -> "startDate: " + startDate);
            }

            @Test
            @DisplayName("종료일이 없으면 OutOfRangeException")
            void nullEndDate() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();

                assertThrows(OutOfRangeException.class, () -> bean(ProjectMakerService.class).createProject(maker.getId(),
                        request(category.getId(), LocalDateTime.now(), null, unique("title"))));
            }

            @Test
            @DisplayName("종료일이 시작일과 같으면 OutOfRangeException이고 저장되지 않는다")
            void sameStartAndEnd() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                LocalDateTime date = LocalDateTime.now().plusDays(3);

                assertThrows(OutOfRangeException.class, () -> bean(ProjectMakerService.class).createProject(maker.getId(),
                        request(category.getId(), date, date, unique("title"))));
                assertTrue(bean(ProjectRepository.class).findByUserId(maker.getId()).isEmpty());
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void unknownMaker() {
                Category category = ProjectFixtures.category();

                assertThrows(NotFoundException.class, () -> bean(ProjectMakerService.class).createProject(Long.MAX_VALUE,
                        request(category.getId(), null, LocalDateTime.now().plusDays(3), unique("title"))));
            }

            @Test
            @DisplayName("없는 카테고리면 NotFoundException")
            void unknownCategory() {
                AppUser maker = createUser();

                assertThrows(NotFoundException.class, () -> bean(ProjectMakerService.class).createProject(maker.getId(),
                        request(Long.MAX_VALUE, null, LocalDateTime.now().plusDays(3), unique("title"))));
            }
        }
    }

    // ===================================================================== GET /project/{project_id}

    @Nested
    @ApiEndpoint(method = "GET", path = "/project/{project_id}")
    @DisplayName("GET /project/{project_id} - 프로젝트 단건 조회")
    class GetProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 프로젝트 정보를 돌려준다")
            void returnsProject() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project saved = ProjectFixtures.project(maker, category, ProjectState.APPROVED);

                ApiResponse response = client.get("/project/" + saved.getId()).send().assertStatus(200);

                JsonNode data = response.json("/data");
                assertEquals(saved.getId(), data.path("id").asLong());
                assertEquals(maker.getId(), data.path("makerId").asLong());
                assertEquals(category.getId(), data.path("categoryId").asLong());
                assertEquals(saved.getTitle(), data.path("title").asText());
                assertEquals(saved.getText(), data.path("text").asText());
                assertEquals("APPROVED", data.path("state").asText());
                assertEquals(0L, data.path("likeCount").asLong());
                assertEquals(0L, data.path("dislikeCount").asLong());
                assertEquals(0, new BigDecimal("10000").compareTo(data.path("purposeAmount").decimalValue()));
                assertTrue(data.has("startDate") && data.has("endDate"), response::toString);
            }

            @Test
            @DisplayName("이미지는 Base64 문자열로 내려간다")
            void imageAsBase64() {
                byte[] img = "image-bytes".getBytes();
                Project saved = ProjectFixtures.project(Project.builder()
                        .maker(createUser())
                        .title(unique("project"))
                        .imgFile(img)
                        .endDate(LocalDateTime.now().plusDays(3)));

                ApiResponse response = client.get("/project/" + saved.getId()).send().assertStatus(200);

                assertEquals(Base64.getEncoder().encodeToString(img), response.json("/data/imgFile").asText());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                client.get("/project/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.get("/project/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("도메인 값을 그대로 DTO로 옮긴다")
            void mapsFields() {
                AppUser maker = createUser();
                Project saved = ProjectFixtures.projectWithLikes(maker, ProjectState.IN_PROGRESS, 4, 2);

                ProjectData data = bean(ProjectQueryService.class).getProject(saved.getId()).getData();

                assertEquals(saved.getId(), data.getId());
                assertEquals(maker.getId(), data.getMakerId());
                assertNull(data.getCategoryId());
                assertEquals(saved.getTitle(), data.getTitle());
                assertEquals(ProjectState.IN_PROGRESS, data.getState());
                assertEquals(4L, data.getLikeCount());
                assertEquals(2L, data.getDislikeCount());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(ProjectQueryService.class).getProject(Long.MAX_VALUE));
            }
        }
    }

    // ===================================================================== GET /project

    @Nested
    @ApiEndpoint(method = "GET", path = "/project")
    @DisplayName("GET /project - 전체 프로젝트 조회")
    class GetAllProjects {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("limit 없이 로그인 없이 200과 방금 만든 프로젝트가 들어간 목록을 돌려준다")
            void returnsAll() {
                Project saved = project(createUser(), ProjectState.APPROVED);

                ApiResponse response = client.get("/project").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(ids(datas).contains(saved.getId()), () -> "방금 만든 프로젝트가 응답에 없다: " + response);
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                AppUser maker = createUser();
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);

                ApiResponse response = client.get("/project").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/datas").size(), response::toString);
                assertEquals(1L, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void invalidLimit() {
                client.get("/project").query("limit", "many").send().assertStatus(422);
            }

            @Test
            @DisplayName("허용하지 않은 메서드는 405")
            void wrongMethod() {
                client.put("/project").send().assertStatus(405);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit이 null이면 DB의 모든 프로젝트를 돌려준다")
            void nullLimitReturnsAll() {
                project(createUser(), ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = bean(ProjectQueryService.class).getAllProject((Integer) null);

                assertEquals(bean(ProjectRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("limit이 있으면 그 수만큼 자른다")
            void limitCuts() {
                AppUser maker = createUser();
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = bean(ProjectQueryService.class).getAllProject(2);

                assertEquals(2L, result.getAmount());
                assertEquals(2, result.getDatas().size());
            }
        }
    }

    // ===================================================================== GET /project/search

    @Nested
    @ApiEndpoint(method = "GET", path = "/project/search")
    @DisplayName("GET /project/search - 프로젝트 필터 조회")
    class SearchProjects {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("maker_name으로 거르면 그 메이커의 프로젝트만 돌려준다")
            void byMaker() {
                AppUser maker = createUser();
                Project a = project(maker, ProjectState.APPROVED);
                Project b = project(maker, ProjectState.IN_PROGRESS);
                project(createUser(), ProjectState.APPROVED);

                ApiResponse response = client.get("/project/search")
                        .query("maker_name", maker.getNickname())
                        .send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(Set.of(a.getId(), b.getId()), Set.copyOf(ids(datas)), response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                datas.forEach(node -> assertEquals(maker.getId(), node.path("makerId").asLong()));
            }

            @Test
            @DisplayName("state_names를 여러 번 주면 그 상태들만 돌려준다")
            void byMultipleStates() {
                AppUser maker = createUser();
                Project approved = project(maker, ProjectState.APPROVED);
                Project inProgress = project(maker, ProjectState.IN_PROGRESS);
                project(maker, ProjectState.COMPLETED);

                ApiResponse response = client.get("/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("state_names", "APPROVED")
                        .query("state_names", "IN_PROGRESS")
                        .send().assertStatus(200);

                assertEquals(Set.of(approved.getId(), inProgress.getId()), Set.copyOf(ids(response.json("/datas"))),
                        response::toString);
            }

            @Test
            @DisplayName("title은 같은 제목의 프로젝트만 돌려준다")
            void byTitle() {
                Project saved = project(createUser(), ProjectState.APPROVED);

                ApiResponse response = client.get("/project/search")
                        .query("title", saved.getTitle())
                        .send().assertStatus(200);

                assertEquals(List.of(saved.getId()), ids(response.json("/datas")), response::toString);
                assertEquals(saved.getTitle(), response.json("/datas/0/title").asText());
            }

            @Test
            @DisplayName("search_type=DESC_LIKE_COUNT면 좋아요 많은 순으로 정렬한다")
            void sortByLike() {
                AppUser maker = createUser();
                Project low = ProjectFixtures.projectWithLikes(maker, ProjectState.APPROVED, 1, 0);
                Project high = ProjectFixtures.projectWithLikes(maker, ProjectState.APPROVED, 9, 0);
                Project mid = ProjectFixtures.projectWithLikes(maker, ProjectState.APPROVED, 5, 0);

                ApiResponse response = client.get("/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("search_type", "DESC_LIKE_COUNT")
                        .send().assertStatus(200);

                assertEquals(List.of(high.getId(), mid.getId(), low.getId()), ids(response.json("/datas")),
                        response::toString);
            }

            @Test
            @DisplayName("limit만큼만 돌려준다")
            void limit() {
                AppUser maker = createUser();
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);

                ApiResponse response = client.get("/project/search")
                        .query("maker_name", maker.getNickname())
                        .query("limit", 2)
                        .send().assertStatus(200);

                assertEquals(2, response.json("/datas").size(), response::toString);
            }

            @Test
            @DisplayName("없는 search_type이면 400")
            void invalidSearchType() {
                client.get("/project/search").query("search_type", "RANDOM").send().assertStatus(400);
            }

            @Test
            @DisplayName("공개 조회가 허용되지 않은 상태(PENDING_APPROVAL)를 주면 400")
            void disallowedState() {
                client.get("/project/search").query("state_names", "PENDING_APPROVAL").send().assertStatus(400);
            }

            @Test
            @DisplayName("마감임박순(ASC_END_DATE)에 진행중이 아닌 상태를 주면 400")
            void endDateSortNeedsInProgress() {
                client.get("/project/search")
                        .query("search_type", "ASC_END_DATE")
                        .query("state_names", "APPROVED")
                        .send().assertStatus(400);
            }

            @Test
            @DisplayName("없는 메이커 닉네임이면 404")
            void unknownMaker() {
                client.get("/project/search").query("maker_name", unique("nobody")).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 카테고리 이름이면 404")
            void unknownCategory() {
                client.get("/project/search").query("category_name", unique("nocategory")).send().assertStatus(404);
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void invalidLimit() {
                client.get("/project/search").query("limit", "x").send().assertStatus(422);
            }

            @Test
            @DisplayName("빈 문자열 query는 422")
            void emptyQueryValue() {
                client.get("/project/search").query("title", "").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            private DataCollectionResponse<ProjectData> search(String title, String makerName, String categoryName,
                                                               List<String> states, Integer limit, String searchType) {
                return bean(ProjectQueryService.class).getAllProject(title, makerName, categoryName, states, limit, searchType);
            }

            private Project projectAt(AppUser maker, LocalDateTime createdAt, LocalDateTime endDate, BigDecimal donateAmount) {
                return ProjectFixtures.project(Project.builder()
                        .maker(maker)
                        .title(unique("project"))
                        .state(ProjectState.IN_PROGRESS)
                        .createdAt(createdAt)
                        .endDate(endDate)
                        .donateAmount(donateAmount));
            }

            @Test
            @DisplayName("search_type이 없으면 최신순(DESC_CREATE_AT)으로 정렬한다")
            void defaultSortIsNewestFirst() {
                AppUser maker = createUser();
                LocalDateTime now = LocalDateTime.now();
                Project oldest = projectAt(maker, now.minusDays(3), now.plusDays(1), BigDecimal.ZERO);
                Project newest = projectAt(maker, now.minusDays(1), now.plusDays(1), BigDecimal.ZERO);
                Project middle = projectAt(maker, now.minusDays(2), now.plusDays(1), BigDecimal.ZERO);

                DataCollectionResponse<ProjectData> result = search(null, maker.getNickname(), null, null, null, null);

                assertEquals(List.of(newest.getId(), middle.getId(), oldest.getId()), ids(result));
            }

            @Test
            @DisplayName("ASC_CREATE_AT면 오래된 순으로 정렬한다")
            void ascCreatedAt() {
                AppUser maker = createUser();
                LocalDateTime now = LocalDateTime.now();
                Project newest = projectAt(maker, now.minusDays(1), now.plusDays(1), BigDecimal.ZERO);
                Project oldest = projectAt(maker, now.minusDays(3), now.plusDays(1), BigDecimal.ZERO);

                DataCollectionResponse<ProjectData> result =
                        search(null, maker.getNickname(), null, List.of(), null, "ASC_CREATE_AT");

                assertEquals(List.of(oldest.getId(), newest.getId()), ids(result));
            }

            @Test
            @DisplayName("ASC_END_DATE에 IN_PROGRESS를 주면 마감이 가까운 순으로 정렬한다")
            void ascEndDate() {
                AppUser maker = createUser();
                LocalDateTime now = LocalDateTime.now();
                Project late = projectAt(maker, now, now.plusDays(30), BigDecimal.ZERO);
                Project soon = projectAt(maker, now, now.plusDays(2), BigDecimal.ZERO);
                Project mid = projectAt(maker, now, now.plusDays(10), BigDecimal.ZERO);

                DataCollectionResponse<ProjectData> result =
                        search(null, maker.getNickname(), null, List.of("IN_PROGRESS"), null, "ASC_END_DATE");

                assertEquals(List.of(soon.getId(), mid.getId(), late.getId()), ids(result));
            }

            @Test
            @DisplayName("DESC_AMOUNT면 모금액이 큰 순으로 정렬한다")
            void descAmount() {
                AppUser maker = createUser();
                LocalDateTime now = LocalDateTime.now();
                Project small = projectAt(maker, now, now.plusDays(3), new BigDecimal("100"));
                Project big = projectAt(maker, now, now.plusDays(3), new BigDecimal("5000"));

                DataCollectionResponse<ProjectData> result =
                        search(null, maker.getNickname(), null, null, null, "DESC_AMOUNT");

                assertEquals(List.of(big.getId(), small.getId()), ids(result));
            }

            @Test
            @DisplayName("ASC_END_DATE에 IN_PROGRESS 외의 상태가 섞이면 InvalidStateException")
            void ascEndDateRejectsOtherStates() {
                assertThrows(InvalidStateException.class,
                        () -> search(null, null, null, List.of("IN_PROGRESS", "COMPLETED"), null, "ASC_END_DATE"));
            }

            @Test
            @DisplayName("JUST_USER_INPUT, PENDING_APPROVAL, REJECTED 상태는 InvalidStateException")
            void disallowedStates() {
                for (String state : List.of("JUST_USER_INPUT", "PENDING_APPROVAL", "REJECTED")) {
                    assertThrows(InvalidStateException.class,
                            () -> search(null, null, null, List.of(state), null, null), state);
                }
            }

            @Test
            @DisplayName("없는 search_type이면 InvalidSearchTypeException")
            void invalidSearchType() {
                assertThrows(InvalidSearchTypeException.class, () -> search(null, null, null, null, null, "NOPE"));
            }

            @Test
            @DisplayName("category_name으로 거르면 그 카테고리의 프로젝트만 돌려준다")
            void byCategory() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project inCategory = ProjectFixtures.project(maker, category, ProjectState.APPROVED);
                ProjectFixtures.project(maker, ProjectFixtures.category(), ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = search(null, null, category.getName(), null, null, null);

                assertEquals(List.of(inCategory.getId()), ids(result));
                assertEquals(category.getId(), result.getDatas().iterator().next().getCategoryId());
            }

            @Test
            @DisplayName("여러 조건을 주면 모두 만족하는 프로젝트만 돌려준다")
            void combinedFilters() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project target = ProjectFixtures.project(maker, category, ProjectState.COMPLETED);
                ProjectFixtures.project(maker, category, ProjectState.APPROVED);
                ProjectFixtures.project(createUser(), category, ProjectState.COMPLETED);

                DataCollectionResponse<ProjectData> result =
                        search(null, maker.getNickname(), category.getName(), List.of("COMPLETED"), null, null);

                assertEquals(List.of(target.getId()), ids(result));
                assertEquals(1L, result.getAmount());
            }

            @Test
            @DisplayName("빈 상태 목록과 공백 이름은 조건으로 쓰지 않는다")
            void blankConditionsIgnored() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = search(saved.getTitle(), " ", " ", List.of(), null, "");

                assertEquals(List.of(saved.getId()), ids(result));
            }

            @Test
            @DisplayName("limit만큼 자른다")
            void limit() {
                AppUser maker = createUser();
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);
                project(maker, ProjectState.APPROVED);

                DataCollectionResponse<ProjectData> result = search(null, maker.getNickname(), null, null, 2, null);

                assertEquals(2L, result.getAmount());
            }

            @Test
            @DisplayName("없는 메이커/카테고리 이름이면 NotFoundException")
            void unknownNames() {
                assertThrows(NotFoundException.class, () -> search(null, unique("nobody"), null, null, null, null));
                assertThrows(NotFoundException.class, () -> search(null, null, unique("nocat"), null, null, null));
            }
        }
    }

    // ===================================================================== GET /project/my-donate

    @Nested
    @ApiEndpoint(method = "GET", path = "/project/my-donate")
    @DisplayName("GET /project/my-donate - 내가 후원한 프로젝트 조회")
    class GetMyDonateProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("후원한 프로젝트 목록을 200으로 돌려준다")
            void returnsDonatedProjects() {
                AppUser maker = createUser();
                AppUser donor = createUser();
                Project first = project(maker, ProjectState.IN_PROGRESS);
                Project second = project(maker, ProjectState.IN_PROGRESS);
                project(maker, ProjectState.IN_PROGRESS);
                ProjectFixtures.donate(donor, ProjectFixtures.reward(first));
                ProjectFixtures.donate(donor, ProjectFixtures.reward(second));

                ApiResponse response = client.get("/project/my-donate").bearer(login(donor)).send().assertStatus(200);

                assertEquals(Set.of(first.getId(), second.getId()), Set.copyOf(ids(response.json("/datas"))),
                        response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                assertEquals(first.getTitle(), StreamSupport.stream(response.json("/datas").spliterator(), false)
                        .filter(node -> node.path("id").asLong() == first.getId())
                        .findFirst().orElseThrow().path("title").asText());
            }

            @Test
            @DisplayName("후원하지 않은 유저는 빈 목록")
            void emptyForNonDonor() {
                AppUser maker = createUser();
                ProjectFixtures.donate(createUser(), ProjectFixtures.reward(project(maker, ProjectState.IN_PROGRESS)));

                ApiResponse response = client.get("/project/my-donate").bearer(login(maker)).send().assertStatus(200);

                assertEquals(0, response.json("/datas").size(), response::toString);
                assertEquals(0L, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/project/my-donate").send().assertStatus(401);
            }

            @Test
            @DisplayName("Bearer가 아닌 스킴이면 401")
            void wrongScheme() {
                client.get("/project/my-donate").header("Authorization", "Basic abc").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("후원 내역의 리워드가 속한 프로젝트를 돌려준다")
            void mapsDonateToProject() {
                AppUser donor = createUser();
                Project saved = project(createUser(), ProjectState.IN_PROGRESS);
                ProjectFixtures.donate(donor, ProjectFixtures.reward(saved));

                DataCollectionResponse<ProjectData> result = bean(ProjectQueryService.class).getUserDonateProject(donor.getId());

                assertEquals(List.of(saved.getId()), ids(result));
                assertEquals(1L, result.getAmount());
            }

            @Test
            @DisplayName("다른 유저의 후원은 섞이지 않는다")
            void otherUsersExcluded() {
                AppUser donor = createUser();
                Project mine = project(createUser(), ProjectState.IN_PROGRESS);
                Project others = project(createUser(), ProjectState.IN_PROGRESS);
                ProjectFixtures.donate(donor, ProjectFixtures.reward(mine));
                ProjectFixtures.donate(createUser(), ProjectFixtures.reward(others));

                List<Long> result = ids(bean(ProjectQueryService.class).getUserDonateProject(donor.getId()));

                assertEquals(List.of(mine.getId()), result);
            }

            @Test
            @DisplayName("후원이 없으면 빈 목록")
            void empty() {
                DataCollectionResponse<ProjectData> result =
                        bean(ProjectQueryService.class).getUserDonateProject(createUser().getId());

                assertEquals(0L, result.getAmount());
                assertTrue(result.getDatas().isEmpty());
            }
        }
    }

    // ===================================================================== GET /project/my

    @Nested
    @ApiEndpoint(method = "GET", path = "/project/my")
    @DisplayName("GET /project/my - 내가 만든 프로젝트 조회")
    class GetMyCreatedProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("내가 만든 프로젝트만 200으로 돌려준다 (상태 무관)")
            void returnsOwnProjects() {
                AppUser maker = createUser();
                Project a = project(maker, ProjectState.JUST_USER_INPUT);
                Project b = project(maker, ProjectState.REJECTED);
                project(createUser(), ProjectState.APPROVED);

                ApiResponse response = client.get("/project/my").bearer(login(maker)).send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(Set.of(a.getId(), b.getId()), Set.copyOf(ids(datas)), response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                datas.forEach(node -> assertEquals(maker.getId(), node.path("makerId").asLong()));
            }

            @Test
            @DisplayName("만든 프로젝트가 없으면 빈 목록")
            void empty() {
                ApiResponse response = client.get("/project/my").bearer(login(createUser())).send().assertStatus(200);

                assertEquals(0, response.json("/datas").size(), response::toString);
                assertEquals(0L, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/project/my").send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.get("/project/my").bearer("not-a-jwt").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("메이커 id로 만든 프로젝트를 모두 돌려준다")
            void returnsAllOfMaker() {
                AppUser maker = createUser();
                List<Long> created = new ArrayList<>();
                for (ProjectState state : List.of(ProjectState.APPROVED, ProjectState.PENDING_APPROVAL, ProjectState.FAILED)) {
                    created.add(project(maker, state).getId());
                }

                DataCollectionResponse<ProjectData> result = bean(ProjectMakerService.class).getMakerProject(maker.getId());

                assertEquals(Set.copyOf(created), Set.copyOf(ids(result)));
                assertEquals(3L, result.getAmount());
            }

            @Test
            @DisplayName("없는 유저면 빈 목록")
            void unknownUser() {
                assertEquals(0L, bean(ProjectMakerService.class).getMakerProject(Long.MAX_VALUE).getAmount());
            }
        }
    }

    // ===================================================================== GET /project/my-deny

    @Nested
    @ApiEndpoint(method = "GET", path = "/project/my-deny")
    @DisplayName("GET /project/my-deny - 내 프로젝트 거절 사유 조회")
    class GetMyDenyProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("내 프로젝트의 거절 사유를 200으로 돌려준다")
            void returnsDenyReasons() {
                AppUser maker = createUser();
                Project rejected = project(maker, ProjectState.REJECTED);
                DenyProject deny = ProjectFixtures.deny(rejected, "사진이 부족합니다");
                ProjectFixtures.deny(project(createUser(), ProjectState.REJECTED), "다른 메이커");

                ApiResponse response = client.get("/project/my-deny").bearer(login(maker)).send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(1, datas.size(), response::toString);
                assertEquals(1L, response.json("/amount").asLong());
                assertEquals(deny.getId(), datas.get(0).path("id").asLong());
                assertEquals(rejected.getId(), datas.get(0).path("projectId").asLong());
                assertEquals("사진이 부족합니다", datas.get(0).path("denyReason").asText());
            }

            @Test
            @DisplayName("거절된 적이 없으면 빈 목록")
            void empty() {
                AppUser maker = createUser();
                project(maker, ProjectState.APPROVED);

                ApiResponse response = client.get("/project/my-deny").bearer(login(maker)).send().assertStatus(200);

                assertEquals(0, response.json("/datas").size(), response::toString);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/project/my-deny").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("여러 프로젝트의 거절 사유를 모두 모은다")
            void collectsAllProjects() {
                AppUser maker = createUser();
                Project first = project(maker, ProjectState.REJECTED);
                Project second = project(maker, ProjectState.PENDING_APPROVAL);
                ProjectFixtures.deny(first, "사유1");
                ProjectFixtures.deny(first, "사유2");
                ProjectFixtures.deny(second, "사유3");

                DataCollectionResponse<DenyProjectData> result = bean(ProjectMakerService.class).getAllDenyProjects(maker.getId());

                assertEquals(3L, result.getAmount());
                Map<String, Long> reasonToProject = result.getDatas().stream()
                        .collect(Collectors.toMap(DenyProjectData::getDenyReason, DenyProjectData::getProjectId));
                assertEquals(Map.of("사유1", first.getId(), "사유2", first.getId(), "사유3", second.getId()), reasonToProject);
            }

            @Test
            @DisplayName("프로젝트가 없는 메이커는 빈 목록")
            void makerWithoutProjects() {
                DataCollectionResponse<DenyProjectData> result =
                        bean(ProjectMakerService.class).getAllDenyProjects(createUser().getId());

                assertEquals(0L, result.getAmount());
                assertTrue(result.getDatas().isEmpty());
            }
        }
    }

    // ===================================================================== PATCH /project/{project_id}

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/project/{project_id}")
    @DisplayName("PATCH /project/{project_id} - 프로젝트 수정")
    class UpdateProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("주인이 수정하면 200과 id를 돌려주고 DB에 반영된다")
            void updates() {
                AppUser maker = createUser();
                Project saved = ProjectFixtures.project(maker, ProjectFixtures.category(), ProjectState.JUST_USER_INPUT);
                Category newCategory = ProjectFixtures.category();
                String title = unique("edited");

                ApiResponse response = client.patch("/project/" + saved.getId()).bearer(login(maker))
                        .json(editBody(title, "수정된 본문", newCategory.getId()))
                        .send().assertStatus(200);

                assertEquals(saved.getId(), response.json("/id").asLong());
                Project updated = findProject(saved.getId());
                assertEquals(title, updated.getTitle());
                assertEquals("수정된 본문", updated.getText());
                assertEquals(newCategory.getId(), updated.getCategory().getId());
                assertEquals(ProjectState.JUST_USER_INPUT, updated.getState());

                client.get("/project/" + saved.getId()).send().assertStatus(200);
                assertEquals(title, client.get("/project/" + saved.getId()).send().json("/data/title").asText());
            }

            @Test
            @DisplayName("주인이 아니면 403이고 바뀌지 않는다")
            void notOwner() {
                Project saved = project(createUser(), ProjectState.JUST_USER_INPUT);

                client.patch("/project/" + saved.getId()).bearer(login(createUser()))
                        .json(editBody(unique("hack"), null, ProjectFixtures.category().getId()))
                        .send().assertStatus(403);

                assertEquals(saved.getTitle(), findProject(saved.getId()).getTitle());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                client.patch("/project/" + Long.MAX_VALUE).bearer(login(createUser()))
                        .json(editBody(unique("t"), null, ProjectFixtures.category().getId()))
                        .send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 카테고리로 바꾸려 하면 404이고 바뀌지 않는다")
            void unknownCategory() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.JUST_USER_INPUT);

                client.patch("/project/" + saved.getId()).bearer(login(maker))
                        .json(editBody(unique("t"), null, Long.MAX_VALUE))
                        .send().assertStatus(404);

                assertEquals(saved.getTitle(), findProject(saved.getId()).getTitle());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Project saved = project(createUser(), ProjectState.JUST_USER_INPUT);

                client.patch("/project/" + saved.getId())
                        .json(editBody(unique("t"), null, null))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.JUST_USER_INPUT);

                client.patch("/project/" + saved.getId()).bearer(login(maker)).send().assertStatus(422);
            }

            @Test
            @DisplayName("categoryId 타입이 틀리면 422")
            void invalidBodyType() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.JUST_USER_INPUT);

                client.patch("/project/" + saved.getId()).bearer(login(maker))
                        .rawJson("{\"projectDataDTO\": {\"title\": \"t\"}, \"categoryId\": \"abc\"}")
                        .send().assertStatus(422);
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.patch("/project/abc").bearer(login(createUser()))
                        .json(editBody(unique("t"), null, null))
                        .send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            private EditProjectRequest request(String title, String text, Long categoryId) {
                return EditProjectRequest.builder()
                        .projectDataDTO(EditProjectDataRequest.builder().title(title).text(text).build())
                        .categoryId(categoryId)
                        .build();
            }

            @Test
            @DisplayName("REJECTED 프로젝트를 수정하면 PENDING_APPROVAL로 돌아간다")
            void rejectedBecomesPending() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project saved = ProjectFixtures.project(maker, category, ProjectState.REJECTED);

                bean(ProjectMakerService.class).editProject(maker.getId(), saved.getId(),
                        request(unique("t"), null, category.getId()));

                assertEquals(ProjectState.PENDING_APPROVAL, findProject(saved.getId()).getState());
            }

            @Test
            @DisplayName("REJECTED가 아니면 상태는 그대로다")
            void otherStateKept() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project saved = ProjectFixtures.project(maker, category, ProjectState.APPROVED);

                bean(ProjectMakerService.class).editProject(maker.getId(), saved.getId(),
                        request(unique("t"), null, category.getId()));

                assertEquals(ProjectState.APPROVED, findProject(saved.getId()).getState());
            }

            @Test
            @DisplayName("null인 필드는 기존 값을 유지한다")
            void nullFieldsKept() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project saved = ProjectFixtures.project(maker, category, ProjectState.JUST_USER_INPUT);
                String title = unique("only-title");

                IdResponse result = bean(ProjectMakerService.class).editProject(maker.getId(), saved.getId(),
                        request(title, null, category.getId()));

                Project updated = findProject(saved.getId());
                assertEquals(saved.getId(), result.getId());
                assertEquals(title, updated.getTitle());
                assertEquals(saved.getText(), updated.getText());
            }

            // editProject가 categoryId가 null이어도 dtoMap.put("category", null)을 넣어 카테고리를 지워 버린다
            @Test
            @DisplayName("[BUG] categoryId를 보내지 않으면 기존 카테고리를 유지한다")
            void categoryKeptWhenOmitted() {
                AppUser maker = createUser();
                Category category = ProjectFixtures.category();
                Project saved = ProjectFixtures.project(maker, category, ProjectState.JUST_USER_INPUT);

                bean(ProjectMakerService.class).editProject(maker.getId(), saved.getId(), request(unique("t"), null, null));

                Category after = findProject(saved.getId()).getCategory();
                assertNotNull(after, "카테고리가 지워졌다");
                assertEquals(category.getId(), after.getId());
            }

            @Test
            @DisplayName("주인이 아니면 InvalidUserException")
            void notOwner() {
                Project saved = project(createUser(), ProjectState.JUST_USER_INPUT);
                AppUser other = createUser();

                assertThrows(InvalidUserException.class, () -> bean(ProjectMakerService.class)
                        .editProject(other.getId(), saved.getId(), request(unique("t"), null, null)));
                assertEquals(saved.getTitle(), findProject(saved.getId()).getTitle());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void notFound() {
                AppUser maker = createUser();

                assertThrows(NotFoundException.class, () -> bean(ProjectMakerService.class)
                        .editProject(maker.getId(), Long.MAX_VALUE, request(unique("t"), null, null)));
            }
        }
    }

    // ===================================================================== DELETE /project/my/{project_id}

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/project/my/{project_id}")
    @DisplayName("DELETE /project/my/{project_id} - 내 프로젝트 삭제")
    class DeleteProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("주인이 지우면 200과 result=true, 이후 조회는 404")
            void deletes() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.JUST_USER_INPUT);

                ApiResponse response = client.delete("/project/my/" + saved.getId()).bearer(login(maker))
                        .send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/project/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("주인이 아니면 403이고 남아 있다")
            void notOwner() {
                Project saved = project(createUser(), ProjectState.JUST_USER_INPUT);

                client.delete("/project/my/" + saved.getId()).bearer(login(createUser())).send().assertStatus(403);

                assertTrue(bean(ProjectRepository.class).findById(saved.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                client.delete("/project/my/" + Long.MAX_VALUE).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Project saved = project(createUser(), ProjectState.JUST_USER_INPUT);

                client.delete("/project/my/" + saved.getId()).send().assertStatus(401);

                assertTrue(bean(ProjectRepository.class).findById(saved.getId()).isPresent());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.delete("/project/my/abc").bearer(login(createUser())).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("주인이면 지우고 true를 돌려준다")
            void deletes() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.APPROVED);

                assertTrue(bean(ProjectMakerService.class).deleteMakerSelfProject(maker.getId(), saved.getId()).getResult());
                assertTrue(bean(ProjectRepository.class).findById(saved.getId()).isEmpty());
            }

            @Test
            @DisplayName("리워드가 딸린 프로젝트도 지워진다")
            void deletesWithRewards() {
                AppUser maker = createUser();
                Project saved = project(maker, ProjectState.APPROVED);
                Reward reward = ProjectFixtures.reward(saved);

                assertTrue(bean(ProjectMakerService.class).deleteMakerSelfProject(maker.getId(), saved.getId()).getResult());
                assertTrue(bean(repository.RewardRepository.class).findById(reward.getId()).isEmpty());
            }

            @Test
            @DisplayName("주인이 아니면 InvalidUserException이고 남아 있다")
            void notOwner() {
                Project saved = project(createUser(), ProjectState.APPROVED);
                AppUser other = createUser();

                assertThrows(InvalidUserException.class,
                        () -> bean(ProjectMakerService.class).deleteMakerSelfProject(other.getId(), saved.getId()));
                assertTrue(bean(ProjectRepository.class).findById(saved.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(ProjectMakerService.class).deleteMakerSelfProject(createUser().getId(), Long.MAX_VALUE));
            }
        }
    }
}
