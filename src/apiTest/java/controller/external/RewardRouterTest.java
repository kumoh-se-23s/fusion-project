package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import constant.ProjectState;
import exception.InvalidRequestException;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import fixture.DonateFixtures;
import model.domain.AppUser;
import model.domain.Project;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.RewardRepository;
import service.external.DonateService;
import service.external.RewardService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static fixture.DonateFixtures.PER_AMOUNT;
import static fixture.DonateFixtures.reloadProject;
import static fixture.DonateFixtures.reloadReward;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RewardRouter (/reward)")
class RewardRouterTest extends ApiTest {

    private static final LocalDateTime PROVIDE_DATE = LocalDateTime.of(2027, 3, 1, 10, 0, 0);

    private RewardService rewardService() {
        return bean(RewardService.class);
    }

    /** 리워드 생성 요청 body. 필수 값을 모두 채운다. */
    private Map<String, Object> rewardBody(Long projectId, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("projectFk", projectId);
        body.put("rewardName", name);
        body.put("rewardText", name + " 설명");
        body.put("provideDate", PROVIDE_DATE.toString());
        body.put("rewardPerAmount", new BigDecimal("15000"));
        body.put("rewardMaxQuantity", 5);
        return body;
    }

    private CreateRewardDataRequest.CreateRewardDataRequestBuilder validRequest(Long projectId) {
        return CreateRewardDataRequest.builder()
                .projectFk(projectId)
                .rewardName(unique("reward"))
                .rewardText("설명")
                .provideDate(PROVIDE_DATE)
                .rewardPerAmount(new BigDecimal("15000"))
                .rewardMaxQuantity(5L);
    }

    private static List<Long> rewardIds(JsonNode datas) {
        return StreamSupport.stream(datas.spliterator(), false)
                .map(node -> node.path("rewardId").asLong())
                .toList();
    }

    private static List<Long> rewardIds(DataCollectionResponse<RewardData> result) {
        return result.getDatas().stream().map(RewardData::getRewardId).toList();
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/reward/projectid")
    @DisplayName("POST /reward/projectid - 리워드 생성")
    class CreateReward {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("메이커가 만들면 200과 리워드 ID를 돌려주고 DB에 저장된다")
            void createsReward() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                String name = unique("reward");

                ApiResponse response = client.post("/reward/projectid")
                        .bearer(login(maker))
                        .json(rewardBody(project.getId(), name))
                        .send()
                        .assertStatus(200);

                long rewardId = response.json("/id").asLong();
                Reward saved = bean(RewardRepository.class).findById(rewardId).orElseThrow();
                assertEquals(name, saved.getName());
                assertEquals(name + " 설명", saved.getText());
                assertEquals(project.getId(), saved.getProject().getId());
                assertEquals(PROVIDE_DATE, saved.getProvideDate());
                assertEquals(0, new BigDecimal("15000").compareTo(saved.getRewardPerAmount()));
                assertEquals(5L, saved.getMaxQuantity());
                assertEquals(5L, saved.getRemainQuantity(), "처음 남은 수량은 최대 수량과 같다");
            }

            @Test
            @DisplayName("방금 만든 리워드를 GET /reward/{reward_id}로 볼 수 있다")
            void readBack() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                String name = unique("reward");

                long rewardId = client.post("/reward/projectid").bearer(login(maker))
                        .json(rewardBody(project.getId(), name)).send().assertStatus(200)
                        .json("/id").asLong();

                ApiResponse response = client.get("/reward/" + rewardId).send().assertStatus(200);
                assertEquals(name, response.json("/data/name").asText());
                assertEquals(project.getId(), response.json("/data/projectId").asLong());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Project project = DonateFixtures.project(createUser());

                client.post("/reward/projectid").json(rewardBody(project.getId(), unique("reward")))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                Project project = DonateFixtures.project(createUser());

                client.post("/reward/projectid").bearer("invalid.token.value")
                        .json(rewardBody(project.getId(), unique("reward"))).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/reward/projectid").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("제공 예정일 형식이 틀리면 422")
            void invalidDate() {
                AppUser maker = createUser();
                Map<String, Object> body = rewardBody(DonateFixtures.project(maker).getId(), unique("reward"));
                body.put("provideDate", "내일");

                client.post("/reward/projectid").bearer(login(maker)).json(body).send().assertStatus(422);
            }

            @Test
            @DisplayName("리워드 이름이 없으면 400")
            void missingName() {
                AppUser maker = createUser();
                Map<String, Object> body = rewardBody(DonateFixtures.project(maker).getId(), unique("reward"));
                body.remove("rewardName");

                client.post("/reward/projectid").bearer(login(maker)).json(body).send().assertStatus(400);
            }

            @Test
            @DisplayName("없는 프로젝트이면 404")
            void projectNotFound() {
                client.post("/reward/projectid").bearer(login(createUser()))
                        .json(rewardBody(Long.MAX_VALUE, unique("reward"))).send().assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 메이커가 아닌 사람이 만들면 400이고 저장되지 않는다")
            void notMaker() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                Project project = DonateFixtures.project(createUser());

                client.post("/reward/projectid").bearer(login(createUser()))
                        .json(rewardBody(project.getId(), unique("reward"))).send().assertStatus(400);

                assertTrue(bean(RewardRepository.class).findByProjectId(project.getId()).isEmpty());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("요청한 남은 수량은 무시하고 최대 수량으로 채운다")
            void remainEqualsMax() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);

                Long rewardId = rewardService().createReward(maker.getId(),
                        validRequest(project.getId()).rewardMaxQuantity(7L).rewardRemainQuantity(1L).build()).getId();

                Reward saved = reloadReward(rewardId);
                assertEquals(7L, saved.getMaxQuantity());
                assertEquals(7L, saved.getRemainQuantity());
            }

            @Test
            @DisplayName("입력만 된 프로젝트에 리워드를 만들면 승인 대기로 바뀐다")
            void justInputBecomesPending() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker, ProjectState.JUST_USER_INPUT);

                rewardService().createReward(maker.getId(), validRequest(project.getId()).build());

                assertEquals(ProjectState.PENDING_APPROVAL, reloadProject(project.getId()).getState());
            }

            @Test
            @DisplayName("다른 상태의 프로젝트는 상태가 바뀌지 않는다")
            void otherStateUnchanged() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker, ProjectState.IN_PROGRESS);

                rewardService().createReward(maker.getId(), validRequest(project.getId()).build());

                assertEquals(ProjectState.IN_PROGRESS, reloadProject(project.getId()).getState());
            }

            @Test
            @DisplayName("로그인 사용자나 요청이 null이면 InvalidRequestException")
            void nullArgs() {
                Project project = DonateFixtures.project(createUser());

                assertThrows(InvalidRequestException.class, () -> rewardService().createReward(null, validRequest(project.getId()).build()));
                assertThrows(InvalidRequestException.class, () -> rewardService().createReward(createUser().getId(), null));
            }

            @Test
            @DisplayName("필수 값이 비었거나 범위를 벗어나면 LimitNumberOutException")
            void invalidFields() {
                AppUser maker = createUser();
                Long projectId = DonateFixtures.project(maker).getId();
                List<CreateRewardDataRequest> invalids = List.of(
                        validRequest(projectId).projectFk(null).build(),
                        validRequest(projectId).rewardName(null).build(),
                        validRequest(projectId).rewardName("  ").build(),
                        validRequest(projectId).provideDate(null).build(),
                        validRequest(projectId).rewardPerAmount(null).build(),
                        validRequest(projectId).rewardPerAmount(BigDecimal.ZERO).build(),
                        validRequest(projectId).rewardMaxQuantity(null).build(),
                        validRequest(projectId).rewardMaxQuantity(0L).build());

                for (CreateRewardDataRequest request : invalids) {
                    assertThrows(LimitNumberOutException.class, () -> rewardService().createReward(maker.getId(), request));
                }
                assertTrue(bean(RewardRepository.class).findByProjectId(projectId).isEmpty());
            }

            @Test
            @DisplayName("없는 프로젝트이면 NotFoundException")
            void projectNotFound() {
                assertThrows(NotFoundException.class,
                        () -> rewardService().createReward(createUser().getId(), validRequest(Long.MAX_VALUE).build()));
            }

            @Test
            @DisplayName("메이커가 아니면 InvalidRequestException이고 프로젝트 상태도 그대로다")
            void notMaker() {
                Project project = DonateFixtures.project(createUser(), ProjectState.JUST_USER_INPUT);

                assertThrows(InvalidRequestException.class,
                        () -> rewardService().createReward(createUser().getId(), validRequest(project.getId()).build()));

                assertTrue(bean(RewardRepository.class).findByProjectId(project.getId()).isEmpty());
                assertEquals(ProjectState.JUST_USER_INPUT, reloadProject(project.getId()).getState());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/reward/{reward_id}")
    @DisplayName("GET /reward/{reward_id} - 리워드 단건 조회")
    class GetRewardById {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 리워드 정보를 돌려준다")
            void returnsReward() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward reward = DonateFixtures.reward(project);

                ApiResponse response = client.get("/reward/" + reward.getId()).send().assertStatus(200);

                JsonNode data = response.json("/data");
                assertEquals(reward.getId(), data.path("rewardId").asLong(), response::toString);
                assertEquals(reward.getName(), data.path("name").asText());
                assertEquals(reward.getText(), data.path("text").asText());
                assertEquals(project.getId(), data.path("projectId").asLong());
                assertEquals(maker.getId(), data.path("makerId").asLong());
                assertEquals(0, PER_AMOUNT.compareTo(data.path("rewardPerAmount").decimalValue()));
                assertEquals(DonateFixtures.MAX_QUANTITY, data.path("maxQuantity").asLong());
                assertEquals(DonateFixtures.MAX_QUANTITY, data.path("remainQuantity").asLong());
                assertFalse(data.path("providerDate").isNull(), response::toString);
            }

            @Test
            @DisplayName("없는 리워드이면 404")
            void notFound() {
                client.get("/reward/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("reward_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.get("/reward/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("리워드 필드가 DTO로 옮겨진다")
            void mapsFields() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                RewardData data = rewardService().getRewardById(reward.getId()).getData();

                assertEquals(reward.getId(), data.getRewardId());
                assertEquals(reward.getName(), data.getName());
                assertEquals(reward.getProject().getId(), data.getProjectId());
                assertEquals(reward.getProject().getMaker().getId(), data.getMakerId());
                assertEquals(reward.getProvideDate().withNano(0), data.getProviderDate().withNano(0));
                assertEquals(DonateFixtures.MAX_QUANTITY, data.getRemainQuantity());
            }

            @Test
            @DisplayName("ID가 null이면 LimitNumberOutException, 없으면 NotFoundException")
            void errors() {
                assertThrows(LimitNumberOutException.class, () -> rewardService().getRewardById(null));
                assertThrows(NotFoundException.class, () -> rewardService().getRewardById(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/reward/project/{project_id}")
    @DisplayName("GET /reward/project/{project_id} - 프로젝트별 리워드 조회")
    class GetRewardsByProject {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 그 프로젝트의 리워드만 돌려준다")
            void returnsProjectRewards() {
                Project project = DonateFixtures.project(createUser());
                Reward r1 = DonateFixtures.reward(project);
                Reward r2 = DonateFixtures.reward(project);
                DonateFixtures.rewardOfNewProject();

                ApiResponse response = client.get("/reward/project/" + project.getId()).send().assertStatus(200);

                assertEquals(2L, response.json("/amount").asLong(), response::toString);
                assertEquals(List.of(r1.getId(), r2.getId()).stream().sorted().toList(),
                        rewardIds(response.json("/datas")).stream().sorted().toList());
            }

            @Test
            @DisplayName("리워드가 없는 프로젝트는 빈 목록")
            void empty() {
                Project project = DonateFixtures.project(createUser());

                ApiResponse response = client.get("/reward/project/" + project.getId()).send().assertStatus(200);

                assertEquals(0L, response.json("/amount").asLong());
                assertEquals(0, response.json("/datas").size());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.get("/reward/project/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("프로젝트 리워드를 생성 순서대로 돌려준다")
            void ordered() {
                Project project = DonateFixtures.project(createUser());
                Reward r1 = DonateFixtures.reward(project);
                Reward r2 = DonateFixtures.reward(project);

                DataCollectionResponse<RewardData> result = rewardService().findRewardByProject(project.getId());

                assertEquals(2L, result.getAmount());
                assertEquals(List.of(r1.getId(), r2.getId()), rewardIds(result));
            }

            @Test
            @DisplayName("ID가 null이면 LimitNumberOutException")
            void nullId() {
                assertThrows(LimitNumberOutException.class, () -> rewardService().findRewardByProject(null));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/reward/user_id/{user_id}")
    @DisplayName("GET /reward/user_id/{user_id} - 내가 후원한 리워드 조회")
    class GetRewardByDonate {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("본인 ID로 조회하면 200과 후원한 리워드 목록을 돌려준다")
            void returnsDonatedRewards() {
                AppUser user = createUser();
                Reward r1 = DonateFixtures.rewardOfNewProject();
                Reward r2 = DonateFixtures.rewardOfNewProject();
                DonateFixtures.rewardOfNewProject();
                DonateFixtures.donate(user, r1, PER_AMOUNT, 1L);
                DonateFixtures.donate(user, r2, PER_AMOUNT, 1L);

                ApiResponse response = client.get("/reward/user_id/" + user.getId())
                        .bearer(login(user)).send().assertStatus(200);

                assertEquals(2L, response.json("/amount").asLong(), response::toString);
                assertEquals(List.of(r1.getId(), r2.getId()).stream().sorted().toList(),
                        rewardIds(response.json("/datas")).stream().sorted().toList());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/reward/user_id/1").send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.get("/reward/user_id/1").bearer("invalid.token.value").send().assertStatus(401);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.get("/reward/user_id/abc").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("[BUG] 다른 사람의 ID로 조회하면 400")
            void otherUser() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                AppUser other = createUser();

                client.get("/reward/user_id/" + other.getId()).bearer(login(createUser())).send().assertStatus(400);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("후원이 없으면 빈 목록")
            void empty() {
                AppUser user = createUser();

                DataCollectionResponse<RewardData> result = rewardService().getRewardByDonate(user.getId(), user.getId());

                assertEquals(0L, result.getAmount());
            }

            @Test
            @DisplayName("환불한 후원의 리워드는 빠진다")
            void refundedExcluded() {
                AppUser user = createUser();
                Reward kept = DonateFixtures.rewardOfNewProject();
                Reward refunded = DonateFixtures.rewardOfNewProject();
                DonateFixtures.donate(user, kept, PER_AMOUNT, 1L);
                Long donateId = DonateFixtures.donate(user, refunded, PER_AMOUNT, 1L);
                bean(DonateService.class).refundDonate(donateId, user.getId());

                DataCollectionResponse<RewardData> result = rewardService().getRewardByDonate(user.getId(), user.getId());

                assertEquals(List.of(kept.getId()), rewardIds(result));
            }

            @Test
            @DisplayName("로그인 사용자가 null이거나 다른 사람이면 InvalidRequestException")
            void notSelf() {
                Long userId = createUser().getId();

                assertThrows(InvalidRequestException.class, () -> rewardService().getRewardByDonate(null, userId));
                assertThrows(InvalidRequestException.class,
                        () -> rewardService().getRewardByDonate(createUser().getId(), userId));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/reward")
    @DisplayName("GET /reward - 메이커별 리워드 조회")
    class GetRewardByMaker {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 그 메이커의 모든 프로젝트 리워드를 돌려준다")
            void returnsMakerRewards() {
                AppUser maker = createUser();
                Reward r1 = DonateFixtures.reward(DonateFixtures.project(maker));
                Reward r2 = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.rewardOfNewProject();

                ApiResponse response = client.get("/reward").query("maker_id", maker.getId()).send().assertStatus(200);

                assertEquals(2L, response.json("/amount").asLong(), response::toString);
                assertEquals(List.of(r1.getId(), r2.getId()).stream().sorted().toList(),
                        rewardIds(response.json("/datas")).stream().sorted().toList());
                assertTrue(StreamSupport.stream(response.json("/datas").spliterator(), false)
                        .allMatch(node -> node.path("makerId").asLong() == maker.getId()));
            }

            @Test
            @DisplayName("maker_id가 없으면 422")
            void missingMakerId() {
                client.get("/reward").send().assertStatus(422);
            }

            @Test
            @DisplayName("maker_id가 숫자가 아니면 422")
            void invalidQueryType() {
                client.get("/reward").query("maker_id", "abc").send().assertStatus(422);
            }

            @Test
            @DisplayName("프로젝트가 없는 메이커는 빈 목록")
            void empty() {
                ApiResponse response = client.get("/reward").query("maker_id", createUser().getId()).send().assertStatus(200);

                assertEquals(0L, response.json("/amount").asLong());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("리워드가 없는 프로젝트가 있어도 다른 프로젝트의 리워드는 모은다")
            void skipsEmptyProjects() {
                AppUser maker = createUser();
                DonateFixtures.project(maker);
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));

                DataCollectionResponse<RewardData> result = rewardService().getRewardFindByMaker(maker.getId());

                assertEquals(List.of(reward.getId()), rewardIds(result));
            }

            @Test
            @DisplayName("메이커 ID가 null이면 LimitNumberOutException")
            void nullId() {
                assertThrows(LimitNumberOutException.class, () -> rewardService().getRewardFindByMaker(null));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/reward/{reward_id}")
    @DisplayName("PATCH /reward/{reward_id} - 리워드 수정")
    class UpdateReward {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("메이커가 수정하면 200이고 보낸 값만 바뀐다")
            void updates() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                String newName = unique("renamed");

                client.patch("/reward/" + reward.getId())
                        .bearer(login(maker))
                        .json(Map.of("rewardName", newName, "rewardPerAmount", 20000))
                        .send()
                        .assertStatus(200);

                Reward saved = reloadReward(reward.getId());
                assertEquals(newName, saved.getName());
                assertEquals(0, new BigDecimal("20000").compareTo(saved.getRewardPerAmount()));
                assertEquals(reward.getText(), saved.getText(), "보내지 않은 값은 그대로");
                assertEquals(DonateFixtures.MAX_QUANTITY, saved.getMaxQuantity());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.patch("/reward/" + reward.getId()).json(Map.of("rewardName", "x")).send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.patch("/reward/1").bearer("invalid.token.value").json(Map.of("rewardName", "x"))
                        .send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));

                client.patch("/reward/" + reward.getId()).bearer(login(maker)).send().assertStatus(422);
            }

            @Test
            @DisplayName("reward_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.patch("/reward/abc").bearer(login(createUser())).json(Map.of("rewardName", "x"))
                        .send().assertStatus(422);
            }

            @Test
            @DisplayName("없는 리워드이면 404")
            void notFound() {
                client.patch("/reward/" + Long.MAX_VALUE).bearer(login(createUser()))
                        .json(Map.of("rewardName", "x")).send().assertStatus(404);
            }

            @Test
            @DisplayName("이미 신청된 수량보다 적게 줄이면 400이고 그대로다")
            void belowSelected() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT.multiply(BigDecimal.valueOf(3)), 3L);

                client.patch("/reward/" + reward.getId()).bearer(login(maker))
                        .json(Map.of("rewardMaxQuantity", 2)).send().assertStatus(400);

                assertEquals(DonateFixtures.MAX_QUANTITY, reloadReward(reward.getId()).getMaxQuantity());
            }

            @Test
            @DisplayName("[BUG] 메이커가 아닌 사람이 수정하면 400이고 그대로다")
            void notMaker() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.patch("/reward/" + reward.getId()).bearer(login(createUser()))
                        .json(Map.of("rewardName", unique("hijack"))).send().assertStatus(400);

                assertEquals(reward.getName(), reloadReward(reward.getId()).getName());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("최대 수량을 바꾸면 이미 신청된 수량을 뺀 만큼 남은 수량이 된다")
            void maxQuantityKeepsSelected() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT.multiply(BigDecimal.valueOf(3)), 3L);

                rewardService().updateReward(maker.getId(), reward.getId(),
                        CreateRewardDataRequest.builder().rewardMaxQuantity(20L).build());

                Reward saved = reloadReward(reward.getId());
                assertEquals(20L, saved.getMaxQuantity());
                assertEquals(17L, saved.getRemainQuantity());
            }

            @Test
            @DisplayName("신청된 수량과 같게 줄이면 남은 수량이 0이 된다")
            void maxEqualsSelected() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT.multiply(BigDecimal.valueOf(3)), 3L);

                rewardService().updateReward(maker.getId(), reward.getId(),
                        CreateRewardDataRequest.builder().rewardMaxQuantity(3L).build());

                Reward saved = reloadReward(reward.getId());
                assertEquals(3L, saved.getMaxQuantity());
                assertEquals(0L, saved.getRemainQuantity());
            }

            @Test
            @DisplayName("이름/설명/제공일/단가를 바꾸고 수정 시각을 갱신한다")
            void updatesFields() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                LocalDateTime newDate = LocalDateTime.of(2028, 1, 2, 3, 4, 5);

                rewardService().updateReward(maker.getId(), reward.getId(), CreateRewardDataRequest.builder()
                        .rewardName("새 이름")
                        .rewardText("새 설명")
                        .provideDate(newDate)
                        .rewardPerAmount(new BigDecimal("777"))
                        .projectFk(reward.getProject().getId())
                        .build());

                Reward saved = reloadReward(reward.getId());
                assertEquals("새 이름", saved.getName());
                assertEquals("새 설명", saved.getText());
                assertEquals(newDate, saved.getProvideDate());
                assertEquals(0, new BigDecimal("777").compareTo(saved.getRewardPerAmount()));
                assertFalse(saved.getUpdatedAt().isBefore(reward.getUpdatedAt().withNano(0)));
            }

            @Test
            @DisplayName("빈 요청이면 아무것도 바꾸지 않는다")
            void emptyRequest() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));

                assertDoesNotThrow(() -> rewardService().updateReward(maker.getId(), reward.getId(),
                        CreateRewardDataRequest.builder().build()));

                Reward saved = reloadReward(reward.getId());
                assertEquals(reward.getName(), saved.getName());
                assertEquals(reward.getVersion(), saved.getVersion());
            }

            @Test
            @DisplayName("인자가 null이면 InvalidRequestException")
            void nullArgs() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                CreateRewardDataRequest request = CreateRewardDataRequest.builder().build();

                assertThrows(InvalidRequestException.class, () -> rewardService().updateReward(null, reward.getId(), request));
                assertThrows(InvalidRequestException.class, () -> rewardService().updateReward(maker.getId(), null, request));
                assertThrows(InvalidRequestException.class, () -> rewardService().updateReward(maker.getId(), reward.getId(), null));
            }

            @Test
            @DisplayName("없는 리워드이면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> rewardService().updateReward(createUser().getId(),
                        Long.MAX_VALUE, CreateRewardDataRequest.builder().rewardName("x").build()));
            }

            @Test
            @DisplayName("메이커가 아니면 InvalidRequestException")
            void notMaker() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(InvalidRequestException.class, () -> rewardService().updateReward(createUser().getId(),
                        reward.getId(), CreateRewardDataRequest.builder().rewardName("x").build()));
                assertEquals(reward.getName(), reloadReward(reward.getId()).getName());
            }

            @Test
            @DisplayName("다른 프로젝트로 옮기려 하면 InvalidRequestException")
            void otherProject() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                Project other = DonateFixtures.project(maker);

                assertThrows(InvalidRequestException.class, () -> rewardService().updateReward(maker.getId(),
                        reward.getId(), CreateRewardDataRequest.builder().projectFk(other.getId()).build()));
                assertEquals(reward.getProject().getId(), reloadReward(reward.getId()).getProject().getId());
            }

            @Test
            @DisplayName("빈 이름, 0원 이하 단가, 0 이하 수량, 신청 수량보다 적은 수량은 LimitNumberOutException")
            void invalidValues() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT.multiply(BigDecimal.valueOf(3)), 3L);
                List<CreateRewardDataRequest> invalids = List.of(
                        CreateRewardDataRequest.builder().rewardName(" ").build(),
                        CreateRewardDataRequest.builder().rewardPerAmount(BigDecimal.ZERO).build(),
                        CreateRewardDataRequest.builder().rewardMaxQuantity(0L).build(),
                        CreateRewardDataRequest.builder().rewardMaxQuantity(2L).build());

                for (CreateRewardDataRequest request : invalids) {
                    assertThrows(LimitNumberOutException.class,
                            () -> rewardService().updateReward(maker.getId(), reward.getId(), request));
                }

                Reward saved = reloadReward(reward.getId());
                assertEquals(reward.getName(), saved.getName());
                assertEquals(DonateFixtures.MAX_QUANTITY, saved.getMaxQuantity());
                assertEquals(DonateFixtures.MAX_QUANTITY - 3, saved.getRemainQuantity());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/reward/{reward_id}")
    @DisplayName("DELETE /reward/{reward_id} - 리워드 삭제")
    class DeleteReward {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("메이커가 후원 없는 리워드를 지우면 200이고 이후 조회는 404")
            void deletes() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));

                client.delete("/reward/" + reward.getId()).bearer(login(maker)).send().assertStatus(200);

                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isEmpty());
                client.get("/reward/" + reward.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401이고 리워드는 그대로다")
            void noToken() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.delete("/reward/" + reward.getId()).send().assertStatus(401);

                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isPresent());
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.delete("/reward/1").bearer("invalid.token.value").send().assertStatus(401);
            }

            @Test
            @DisplayName("reward_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.delete("/reward/abc").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("없는 리워드이면 404")
            void notFound() {
                client.delete("/reward/" + Long.MAX_VALUE).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 메이커가 아닌 사람이 지우려 하면 400이고 그대로다")
            void notMaker() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.delete("/reward/" + reward.getId()).bearer(login(createUser())).send().assertStatus(400);

                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isPresent());
            }

            @Test
            @DisplayName("[BUG] 후원이 있는 리워드를 지우려 하면 400이고 그대로다")
            void hasDonates() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT, 1L);

                client.delete("/reward/" + reward.getId()).bearer(login(maker)).send().assertStatus(400);

                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isPresent());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("후원이 없으면 지워지고 같은 프로젝트의 다른 리워드는 남는다")
            void deletesOnlyTarget() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward target = DonateFixtures.reward(project);
                Reward other = DonateFixtures.reward(project);

                rewardService().deleteReward(maker.getId(), target.getId());

                assertTrue(bean(RewardRepository.class).findById(target.getId()).isEmpty());
                assertTrue(bean(RewardRepository.class).findById(other.getId()).isPresent());
            }

            @Test
            @DisplayName("후원을 모두 환불한 리워드는 지울 수 있다")
            void deletableAfterRefund() {
                AppUser maker = createUser();
                AppUser user = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                Long donateId = DonateFixtures.donate(user, reward, PER_AMOUNT, 1L);
                bean(DonateService.class).refundDonate(donateId, user.getId());

                rewardService().deleteReward(maker.getId(), reward.getId());

                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isEmpty());
            }

            @Test
            @DisplayName("후원이 있으면 InvalidRequestException")
            void hasDonates() {
                AppUser maker = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(maker));
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT, 1L);

                assertThrows(InvalidRequestException.class, () -> rewardService().deleteReward(maker.getId(), reward.getId()));
                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isPresent());
            }

            @Test
            @DisplayName("인자가 null이면 InvalidRequestException, 없는 리워드면 NotFoundException")
            void argsAndNotFound() {
                Long userId = createUser().getId();

                assertThrows(InvalidRequestException.class, () -> rewardService().deleteReward(null, 1L));
                assertThrows(InvalidRequestException.class, () -> rewardService().deleteReward(userId, null));
                assertThrows(NotFoundException.class, () -> rewardService().deleteReward(userId, Long.MAX_VALUE));
            }

            @Test
            @DisplayName("메이커가 아니면 InvalidRequestException")
            void notMaker() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(InvalidRequestException.class,
                        () -> rewardService().deleteReward(createUser().getId(), reward.getId()));
                assertTrue(bean(RewardRepository.class).findById(reward.getId()).isPresent());
            }
        }
    }
}
