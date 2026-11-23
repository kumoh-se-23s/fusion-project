package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import exception.AlreadyDonatedException;
import exception.InvalidRequestException;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import exception.TransactionException;
import fixture.DonateFixtures;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;
import model.dto.DonateData;
import model.dto.request.donate.CreateDonateRequest;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.DonateRepository;
import repository.ProjectRepository;
import service.external.DonateService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static fixture.DonateFixtures.PER_AMOUNT;
import static fixture.DonateFixtures.reloadProject;
import static fixture.DonateFixtures.reloadReward;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DonateRouter (/donate)")
class DonateRouterTest extends ApiTest {

    private static final BigDecimal TWO_REWARDS = PER_AMOUNT.multiply(BigDecimal.valueOf(2));

    private DonateService donateService() {
        return bean(DonateService.class);
    }

    private Map<String, Object> donateBody(Reward reward, BigDecimal amount, long quantity) {
        return Map.of("rewardFk", reward.getId(), "amount", amount, "quantity", quantity);
    }

    private static boolean containsDonate(JsonNode datas, Long userId, Long rewardId) {
        return StreamSupport.stream(datas.spliterator(), false)
                .anyMatch(node -> node.path("user").path("id").asLong() == userId
                        && node.path("reward").path("id").asLong() == rewardId);
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/donate")
    @DisplayName("POST /donate - 후원 생성")
    class CreateDonate {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인한 사용자가 후원하면 200과 후원 ID를 돌려주고 DB에 저장된다")
            void createsDonate() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                ApiResponse response = client.post("/donate")
                        .bearer(login(user))
                        .json(donateBody(reward, TWO_REWARDS, 2))
                        .send()
                        .assertStatus(200);

                long donateId = response.json("/id").asLong();
                assertTrue(donateId > 0, response::toString);

                Donate saved = bean(DonateRepository.class).findById(donateId).orElseThrow();
                assertEquals(user.getId(), saved.getUser().getId());
                assertEquals(reward.getId(), saved.getReward().getId());
                assertEquals(0, TWO_REWARDS.compareTo(saved.getAmount()));
                assertEquals(2L, saved.getQuantity());
                assertFalse(saved.getState());
            }

            @Test
            @DisplayName("후원하면 리워드 재고가 줄고 프로젝트 누적 금액이 늘어난다")
            void updatesStockAndProjectAmount() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate")
                        .bearer(login(user))
                        .json(donateBody(reward, TWO_REWARDS, 2))
                        .send()
                        .assertStatus(200);

                assertEquals(DonateFixtures.MAX_QUANTITY - 2, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, TWO_REWARDS.compareTo(reloadProject(reward.getProject().getId()).getDonateAmount()));
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate").json(donateBody(reward, PER_AMOUNT, 1)).send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate").bearer("invalid.token.value")
                        .json(donateBody(reward, PER_AMOUNT, 1)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/donate").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("금액이 숫자가 아니면 422")
            void invalidAmountType() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate").bearer(login(createUser()))
                        .rawJson("{\"rewardFk\":" + reward.getId() + ",\"amount\":\"abc\",\"quantity\":1}")
                        .send()
                        .assertStatus(422);
            }

            @Test
            @DisplayName("없는 리워드에 후원하면 404")
            void rewardNotFound() {
                client.post("/donate").bearer(login(createUser()))
                        .json(Map.of("rewardFk", Long.MAX_VALUE, "amount", PER_AMOUNT, "quantity", 1))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("재고보다 많이 신청하면 400이고 재고는 그대로다")
            void outOfStock() {
                Reward reward = DonateFixtures.reward(DonateFixtures.project(createUser()), 1L);

                client.post("/donate").bearer(login(createUser()))
                        .json(donateBody(reward, TWO_REWARDS, 2))
                        .send()
                        .assertStatus(400);

                assertEquals(1L, reloadReward(reward.getId()).getRemainQuantity());
            }

            @Test
            @DisplayName("금액이 0원이면 400")
            void zeroAmount() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate").bearer(login(createUser()))
                        .json(donateBody(reward, BigDecimal.ZERO, 1))
                        .send()
                        .assertStatus(400);
            }

            @Test
            @DisplayName("같은 프로젝트에 두 번 후원하면 409")
            void alreadyDonated() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                String token = login(user);

                client.post("/donate").bearer(token).json(donateBody(reward, PER_AMOUNT, 1)).send().assertStatus(200);
                client.post("/donate").bearer(token).json(donateBody(reward, PER_AMOUNT, 1)).send().assertStatus(409);

                assertEquals(DonateFixtures.MAX_QUANTITY - 1, reloadReward(reward.getId()).getRemainQuantity());
            }

            @Test
            @DisplayName("[BUG] 리워드 ID가 없으면 400")
            void missingRewardId() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                client.post("/donate").bearer(login(createUser()))
                        .json(Map.of("amount", PER_AMOUNT, "quantity", 1))
                        .send()
                        .assertStatus(400);
            }

            @Test
            @DisplayName("[BUG] body의 userFk가 로그인 사용자와 다르면 400이고 저장되지 않는다")
            void otherUserFk() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                AppUser user = createUser();
                AppUser other = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                client.post("/donate").bearer(login(user))
                        .json(Map.of("userFk", other.getId(), "rewardFk", reward.getId(),
                                "amount", PER_AMOUNT, "quantity", 1))
                        .send()
                        .assertStatus(400);

                assertTrue(bean(DonateRepository.class).findByUserId(other.getId()).isEmpty());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("후원 금액만큼 프로젝트 누적 금액이 더해지고 신청 수량만큼 재고가 준다")
            void success() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long projectId = reward.getProject().getId();
                bean(ProjectRepository.class).updateByDonateAmount(projectId, new BigDecimal("5000"));

                Long donateId = donateService().createDonate(user.getId(),
                        new CreateDonateRequest(null, reward.getId(), new BigDecimal("30000"), 3L)).getId();

                Donate saved = bean(DonateRepository.class).findById(donateId).orElseThrow();
                assertEquals(user.getId(), saved.getUser().getId(), "userFk가 없으면 로그인 사용자로 채운다");
                assertEquals(3L, saved.getQuantity());
                assertFalse(saved.getState(), "새 후원은 배송 전 상태");
                assertEquals(DonateFixtures.MAX_QUANTITY - 3, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, new BigDecimal("35000").compareTo(reloadProject(projectId).getDonateAmount()));
            }

            @Test
            @DisplayName("재고를 딱 맞게 모두 신청할 수 있다")
            void takeAllStock() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(createUser()), 2L);

                donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), reward.getId(), TWO_REWARDS, 2L));

                assertEquals(0L, reloadReward(reward.getId()).getRemainQuantity());
            }

            @Test
            @DisplayName("[BUG] 후원 금액은 리워드 단가 x 수량으로 계산된다")
            void amountIsCalculatedFromReward() {
                // TODO 1번(Donate Amount 자동 계산)이 구현되지 않아 요청의 amount를 그대로 저장한다
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                Long donateId = donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), reward.getId(), new BigDecimal("1"), 2L)).getId();

                Donate saved = bean(DonateRepository.class).findById(donateId).orElseThrow();
                assertEquals(0, TWO_REWARDS.compareTo(saved.getAmount()), "저장된 금액: " + saved.getAmount());
            }

            @Test
            @DisplayName("요청이 null이면 InvalidRequestException")
            void nullRequest() {
                assertThrows(InvalidRequestException.class, () -> donateService().createDonate(createUser().getId(), null));
            }

            @Test
            @DisplayName("로그인 사용자 ID가 null이면 InvalidRequestException")
            void nullLoginUser() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(InvalidRequestException.class, () -> donateService().createDonate(null,
                        new CreateDonateRequest(null, reward.getId(), PER_AMOUNT, 1L)));
            }

            @Test
            @DisplayName("userFk가 로그인 사용자와 다르면 InvalidRequestException")
            void otherUserFk() {
                AppUser user = createUser();
                AppUser other = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(InvalidRequestException.class, () -> donateService().createDonate(user.getId(),
                        new CreateDonateRequest(other.getId(), reward.getId(), PER_AMOUNT, 1L)));
                assertEquals(DonateFixtures.MAX_QUANTITY, reloadReward(reward.getId()).getRemainQuantity());
            }

            @Test
            @DisplayName("리워드 ID가 없으면 InvalidRequestException")
            void nullReward() {
                AppUser user = createUser();

                assertThrows(InvalidRequestException.class, () -> donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), null, PER_AMOUNT, 1L)));
            }

            @Test
            @DisplayName("금액이 없거나 0 이하이면 LimitNumberOutException")
            void invalidAmount() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                for (BigDecimal amount : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1")}) {
                    assertThrows(LimitNumberOutException.class, () -> donateService().createDonate(user.getId(),
                            new CreateDonateRequest(user.getId(), reward.getId(), amount, 1L)), "amount=" + amount);
                }
                assertTrue(bean(DonateRepository.class).findByUserId(user.getId()).isEmpty());
            }

            @Test
            @DisplayName("수량이 없거나 0 이하이면 LimitNumberOutException")
            void invalidQuantity() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();

                for (Long quantity : new Long[]{null, 0L, -1L}) {
                    assertThrows(LimitNumberOutException.class, () -> donateService().createDonate(user.getId(),
                            new CreateDonateRequest(user.getId(), reward.getId(), PER_AMOUNT, quantity)), "quantity=" + quantity);
                }
                assertTrue(bean(DonateRepository.class).findByUserId(user.getId()).isEmpty());
            }

            @Test
            @DisplayName("없는 사용자이면 NotFoundException")
            void userNotFound() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(NotFoundException.class, () -> donateService().createDonate(Long.MAX_VALUE,
                        new CreateDonateRequest(null, reward.getId(), PER_AMOUNT, 1L)));
            }

            @Test
            @DisplayName("없는 리워드이면 NotFoundException")
            void rewardNotFound() {
                AppUser user = createUser();

                assertThrows(NotFoundException.class, () -> donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), Long.MAX_VALUE, PER_AMOUNT, 1L)));
            }

            @Test
            @DisplayName("재고보다 많이 신청하면 LimitNumberOutException이고 아무것도 바뀌지 않는다")
            void outOfStock() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(createUser()), 2L);

                assertThrows(LimitNumberOutException.class, () -> donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), reward.getId(), PER_AMOUNT, 3L)));

                assertEquals(2L, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, BigDecimal.ZERO.compareTo(reloadProject(reward.getProject().getId()).getDonateAmount()));
                assertTrue(bean(DonateRepository.class).findByUserId(user.getId()).isEmpty());
            }

            @Test
            @DisplayName("같은 프로젝트의 다른 리워드라도 이미 후원했으면 AlreadyDonatedException")
            void alreadyDonatedSameProject() {
                AppUser user = createUser();
                Project project = DonateFixtures.project(createUser());
                Reward first = DonateFixtures.reward(project);
                Reward second = DonateFixtures.reward(project);
                DonateFixtures.donate(user, first, PER_AMOUNT, 1L);

                assertThrows(AlreadyDonatedException.class, () -> donateService().createDonate(user.getId(),
                        new CreateDonateRequest(user.getId(), second.getId(), PER_AMOUNT, 1L)));

                assertEquals(DonateFixtures.MAX_QUANTITY, reloadReward(second.getId()).getRemainQuantity());
                assertEquals(0, PER_AMOUNT.compareTo(reloadProject(project.getId()).getDonateAmount()));
            }

            @Test
            @DisplayName("다른 프로젝트에는 또 후원할 수 있다")
            void otherProjectAllowed() {
                AppUser user = createUser();
                DonateFixtures.donate(user, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);
                DonateFixtures.donate(user, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                assertEquals(2, bean(DonateRepository.class).findByUserId(user.getId()).size());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/donate")
    @DisplayName("GET /donate - 사용자/리워드/프로젝트별 후원 조회")
    class GetDonates {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("user_id로 본인 후원 목록을 200으로 돌려준다")
            void byUser() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                DonateFixtures.donate(user, reward, PER_AMOUNT, 1L);

                ApiResponse response = client.get("/donate")
                        .bearer(login(user))
                        .query("user_id", user.getId())
                        .send()
                        .assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(1, datas.size(), response::toString);
                assertEquals(1L, response.json("/amount").asLong());
                JsonNode data = datas.get(0);
                assertEquals(user.getId(), data.path("user").path("id").asLong());
                assertEquals(reward.getId(), data.path("reward").path("id").asLong());
                assertEquals(0, PER_AMOUNT.compareTo(data.path("amount").decimalValue()));
                assertEquals(1L, data.path("quantity").asLong());
                assertFalse(data.path("state").asBoolean());
                assertFalse(data.path("createdAt").isNull(), response::toString);
            }

            @Test
            @DisplayName("[BUG] 후원 목록의 각 항목에 환불에 쓸 후원 ID가 들어 있다")
            void containsDonateId() {
                // DonateData에 id 필드가 없어 목록 응답만으로는 PATCH /donate/{donate_id}에 쓸 ID를 알 수 없다
                AppUser user = createUser();
                Long donateId = DonateFixtures.donate(user, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                ApiResponse response = client.get("/donate").bearer(login(user))
                        .query("user_id", user.getId()).send().assertStatus(200);

                assertEquals(donateId, response.json("/datas/0/id").asLong(), response::toString);
            }

            @Test
            @DisplayName("메이커는 project_id로 자기 프로젝트의 후원 목록을 본다")
            void byProjectAsMaker() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward reward = DonateFixtures.reward(project);
                AppUser a = createUser();
                AppUser b = createUser();
                DonateFixtures.donate(a, reward, PER_AMOUNT, 1L);
                DonateFixtures.donate(b, reward, PER_AMOUNT, 1L);

                ApiResponse response = client.get("/donate").bearer(login(maker))
                        .query("project_id", project.getId()).send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(2, datas.size(), response::toString);
                assertTrue(containsDonate(datas, a.getId(), reward.getId()));
                assertTrue(containsDonate(datas, b.getId(), reward.getId()));
            }

            @Test
            @DisplayName("메이커는 reward_id로 리워드의 후원 목록을 본다")
            void byRewardAsMaker() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward reward = DonateFixtures.reward(project);
                Reward otherReward = DonateFixtures.reward(project);
                AppUser a = createUser();
                AppUser b = createUser();
                DonateFixtures.donate(a, reward, PER_AMOUNT, 1L);
                DonateFixtures.donate(b, otherReward, PER_AMOUNT, 1L);

                ApiResponse response = client.get("/donate").bearer(login(maker))
                        .query("reward_id", reward.getId()).send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertEquals(1, datas.size(), response::toString);
                assertTrue(containsDonate(datas, a.getId(), reward.getId()));
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/donate").query("user_id", 1).send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.get("/donate").bearer("invalid.token.value").query("user_id", 1).send().assertStatus(401);
            }

            @Test
            @DisplayName("user_id가 숫자가 아니면 422")
            void invalidQueryType() {
                client.get("/donate").bearer(login(createUser())).query("user_id", "abc").send().assertStatus(422);
            }

            @Test
            @DisplayName("없는 프로젝트이면 404")
            void projectNotFound() {
                client.get("/donate").bearer(login(createUser()))
                        .query("project_id", Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 리워드이면 404")
            void rewardNotFound() {
                client.get("/donate").bearer(login(createUser()))
                        .query("reward_id", Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 조회 조건이 없으면 400")
            void noCondition() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                client.get("/donate").bearer(login(createUser())).send().assertStatus(400);
            }

            @Test
            @DisplayName("[BUG] 다른 사람의 user_id로 조회하면 400")
            void otherUser() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                AppUser other = createUser();
                DonateFixtures.donate(other, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                client.get("/donate").bearer(login(createUser()))
                        .query("user_id", other.getId()).send().assertStatus(400);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("본인 user_id로 조회하면 내 후원만 돌려준다")
            void byUser() {
                AppUser user = createUser();
                Reward r1 = DonateFixtures.rewardOfNewProject();
                Reward r2 = DonateFixtures.rewardOfNewProject();
                DonateFixtures.donate(user, r1, PER_AMOUNT, 1L);
                DonateFixtures.donate(user, r2, TWO_REWARDS, 2L);
                DonateFixtures.donate(createUser(), r1, PER_AMOUNT, 1L);

                DataCollectionResponse<DonateData> result = donateService().getDonates(user.getId(),
                        Optional.of(user.getId()), Optional.empty(), Optional.empty());

                assertEquals(2L, result.getAmount());
                assertTrue(result.getDatas().stream().allMatch(d -> d.getUser().getId().equals(user.getId())));
                assertEquals(List.of(r1.getId(), r2.getId()).stream().sorted().toList(),
                        result.getDatas().stream().map(d -> d.getReward().getId()).sorted().toList());
            }

            @Test
            @DisplayName("후원이 없으면 빈 목록")
            void emptyForUser() {
                AppUser user = createUser();

                DataCollectionResponse<DonateData> result = donateService().getDonates(user.getId(),
                        Optional.of(user.getId()), Optional.empty(), Optional.empty());

                assertEquals(0L, result.getAmount());
                assertTrue(result.getDatas().isEmpty());
            }

            @Test
            @DisplayName("project_id로 조회하면 그 프로젝트의 모든 리워드 후원을 돌려준다")
            void byProject() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward r1 = DonateFixtures.reward(project);
                Reward r2 = DonateFixtures.reward(project);
                DonateFixtures.donate(createUser(), r1, PER_AMOUNT, 1L);
                DonateFixtures.donate(createUser(), r2, PER_AMOUNT, 1L);
                DonateFixtures.donate(createUser(), DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                DataCollectionResponse<DonateData> result = donateService().getDonates(maker.getId(),
                        Optional.empty(), Optional.empty(), Optional.of(project.getId()));

                assertEquals(2L, result.getAmount());
                assertTrue(result.getDatas().stream()
                        .allMatch(d -> d.getReward().getProject().getId().equals(project.getId())));
            }

            @Test
            @DisplayName("reward_id로 조회하면 그 리워드의 후원만 돌려준다")
            void byReward() {
                AppUser maker = createUser();
                Project project = DonateFixtures.project(maker);
                Reward r1 = DonateFixtures.reward(project);
                Reward r2 = DonateFixtures.reward(project);
                DonateFixtures.donate(createUser(), r1, PER_AMOUNT, 1L);
                DonateFixtures.donate(createUser(), r2, PER_AMOUNT, 1L);

                DataCollectionResponse<DonateData> result = donateService().getDonates(maker.getId(),
                        Optional.empty(), Optional.of(r1.getId()), Optional.empty());

                assertEquals(1L, result.getAmount());
                assertEquals(r1.getId(), result.getDatas().iterator().next().getReward().getId());
            }

            @Test
            @DisplayName("조건이 0개이거나 2개 이상이면 InvalidRequestException")
            void conditionCount() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.reward(DonateFixtures.project(user));

                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(user.getId(),
                        Optional.empty(), Optional.empty(), Optional.empty()));
                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(user.getId(),
                        Optional.of(user.getId()), Optional.of(reward.getId()), Optional.empty()));
                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(user.getId(),
                        Optional.of(user.getId()), Optional.of(reward.getId()), Optional.of(reward.getProject().getId())));
            }

            @Test
            @DisplayName("로그인 사용자 ID가 null이면 InvalidRequestException")
            void nullLoginUser() {
                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(null,
                        Optional.of(1L), Optional.empty(), Optional.empty()));
            }

            @Test
            @DisplayName("다른 사람의 user_id로 조회하면 InvalidRequestException")
            void otherUser() {
                AppUser other = createUser();

                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(createUser().getId(),
                        Optional.of(other.getId()), Optional.empty(), Optional.empty()));
            }

            @Test
            @DisplayName("메이커가 아닌 사람이 reward_id로 조회하면 InvalidRequestException")
            void notMakerByReward() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(createUser().getId(),
                        Optional.empty(), Optional.of(reward.getId()), Optional.empty()));
            }

            @Test
            @DisplayName("메이커가 아닌 사람이 project_id로 조회하면 InvalidRequestException")
            void notMakerByProject() {
                Project project = DonateFixtures.project(createUser());

                assertThrows(InvalidRequestException.class, () -> donateService().getDonates(createUser().getId(),
                        Optional.empty(), Optional.empty(), Optional.of(project.getId())));
            }

            @Test
            @DisplayName("없는 리워드/프로젝트이면 NotFoundException")
            void notFound() {
                Long userId = createUser().getId();

                assertThrows(NotFoundException.class, () -> donateService().getDonates(userId,
                        Optional.empty(), Optional.of(Long.MAX_VALUE), Optional.empty()));
                assertThrows(NotFoundException.class, () -> donateService().getDonates(userId,
                        Optional.empty(), Optional.empty(), Optional.of(Long.MAX_VALUE)));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/donate/{donate_id}")
    @DisplayName("PATCH /donate/{donate_id} - 후원 환불")
    class RefundDonate {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("본인 후원을 환불하면 200이고 후원이 지워지며 재고와 누적 금액이 되돌아간다")
            void refunds() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long donateId = DonateFixtures.donate(user, reward, TWO_REWARDS, 2L);

                client.patch("/donate/" + donateId).bearer(login(user)).send().assertStatus(200);

                assertTrue(bean(DonateRepository.class).findById(donateId).isEmpty());
                assertEquals(DonateFixtures.MAX_QUANTITY, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, BigDecimal.ZERO.compareTo(reloadProject(reward.getProject().getId()).getDonateAmount()));
            }

            @Test
            @DisplayName("환불한 뒤에는 같은 프로젝트에 다시 후원할 수 있다")
            void donateAgainAfterRefund() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long donateId = DonateFixtures.donate(user, reward, PER_AMOUNT, 1L);
                String token = login(user);

                client.patch("/donate/" + donateId).bearer(token).send().assertStatus(200);

                client.post("/donate").bearer(token).json(donateBody(reward, PER_AMOUNT, 1)).send().assertStatus(200);
            }

            @Test
            @DisplayName("토큰이 없으면 401이고 후원은 그대로다")
            void noToken() {
                AppUser user = createUser();
                Long donateId = DonateFixtures.donate(user, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                client.patch("/donate/" + donateId).send().assertStatus(401);

                assertTrue(bean(DonateRepository.class).findById(donateId).isPresent());
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.patch("/donate/1").bearer("invalid.token.value").send().assertStatus(401);
            }

            @Test
            @DisplayName("donate_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.patch("/donate/abc").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("없는 후원이면 404")
            void notFound() {
                client.patch("/donate/" + Long.MAX_VALUE).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 다른 사람의 후원을 환불하려 하면 400이고 후원은 그대로다")
            void otherUsersDonate() {
                // InvalidRequestException이 BAD_REQUEST 상수를 선언만 하고 super(message)로 기본값 500을 쓴다
                AppUser owner = createUser();
                Long donateId = DonateFixtures.donate(owner, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                client.patch("/donate/" + donateId).bearer(login(createUser())).send().assertStatus(400);

                assertTrue(bean(DonateRepository.class).findById(donateId).isPresent());
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("후원 수량만큼 재고를 되돌리고 후원 금액만큼 누적 금액을 뺀다")
            void restoresStockAndAmount() {
                AppUser user = createUser();
                Project project = DonateFixtures.project(createUser());
                Reward reward = DonateFixtures.reward(project);
                DonateFixtures.donate(createUser(), reward, PER_AMOUNT, 1L);
                Long donateId = DonateFixtures.donate(user, reward, new BigDecimal("30000"), 3L);

                donateService().refundDonate(donateId, user.getId());

                assertTrue(bean(DonateRepository.class).findById(donateId).isEmpty());
                assertEquals(DonateFixtures.MAX_QUANTITY - 1, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, PER_AMOUNT.compareTo(reloadProject(project.getId()).getDonateAmount()));
            }

            @Test
            @DisplayName("ID가 null이면 InvalidRequestException")
            void nullIds() {
                assertThrows(InvalidRequestException.class, () -> donateService().refundDonate(null, 1L));
                assertThrows(InvalidRequestException.class, () -> donateService().refundDonate(1L, null));
            }

            @Test
            @DisplayName("없는 후원이면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> donateService().refundDonate(Long.MAX_VALUE, createUser().getId()));
            }

            @Test
            @DisplayName("본인 후원이 아니면 InvalidRequestException이고 아무것도 바뀌지 않는다")
            void notOwner() {
                AppUser owner = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long donateId = DonateFixtures.donate(owner, reward, PER_AMOUNT, 1L);

                assertThrows(InvalidRequestException.class,
                        () -> donateService().refundDonate(donateId, createUser().getId()));

                assertTrue(bean(DonateRepository.class).findById(donateId).isPresent());
                assertEquals(DonateFixtures.MAX_QUANTITY - 1, reloadReward(reward.getId()).getRemainQuantity());
                assertEquals(0, PER_AMOUNT.compareTo(reloadProject(reward.getProject().getId()).getDonateAmount()));
            }

            @Test
            @DisplayName("환불 금액이 누적 금액보다 크면 TransactionException이고 후원은 남는다")
            void refundExceedsProjectAmount() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                // 누적 금액에 반영되지 않은 후원 행만 넣는다
                Donate donate = DonateFixtures.donateRow(user, reward, PER_AMOUNT, 1L);

                assertThrows(TransactionException.class, () -> donateService().refundDonate(donate.getId(), user.getId()));

                assertTrue(bean(DonateRepository.class).findById(donate.getId()).isPresent());
                assertEquals(DonateFixtures.MAX_QUANTITY, reloadReward(reward.getId()).getRemainQuantity());
            }
        }
    }
}
