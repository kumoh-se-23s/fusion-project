package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import exception.NotFoundException;
import fixture.DonateFixtures;
import model.domain.AppUser;
import model.domain.Reward;
import model.dto.DonateData;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.DonateRepository;
import service.external.DonateService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static fixture.DonateFixtures.PER_AMOUNT;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminDonateRouter (/internal/donate)")
class AdminDonateRouterTest extends ApiTest {

    private static boolean containsDonate(JsonNode datas, Long userId, Long rewardId) {
        return StreamSupport.stream(datas.spliterator(), false)
                .anyMatch(node -> node.path("user").path("id").asLong() == userId
                        && node.path("reward").path("id").asLong() == rewardId);
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/donate")
    @DisplayName("GET /internal/donate - 관리자 후원 전체/단건 조회")
    class GetDonates {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("donate_id 없이 부르면 200과 내가 만든 후원이 들어 있는 전체 목록")
            void returnsAll() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                DonateFixtures.donate(user, reward, PER_AMOUNT, 1L);

                ApiResponse response = client.get("/internal/donate").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(containsDonate(datas, user.getId(), reward.getId()), () -> "방금 만든 후원이 없다: " + response);
            }

            @Test
            @DisplayName("donate_id로 부르면 그 후원 하나만 돌려준다")
            void returnsOne() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long donateId = DonateFixtures.donate(user, reward, new BigDecimal("20000"), 2L);

                ApiResponse response = client.get("/internal/donate").query("donate_id", donateId).send().assertStatus(200);

                assertEquals(1L, response.json("/amount").asLong(), response::toString);
                JsonNode data = response.json("/datas/0");
                assertEquals(user.getId(), data.path("user").path("id").asLong());
                assertEquals(reward.getId(), data.path("reward").path("id").asLong());
                assertEquals(0, new BigDecimal("20000").compareTo(data.path("amount").decimalValue()));
                assertEquals(2L, data.path("quantity").asLong());
            }

            @Test
            @DisplayName("없는 donate_id이면 404")
            void notFound() {
                client.get("/internal/donate").query("donate_id", Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("donate_id가 숫자가 아니면 422")
            void invalidQueryType() {
                client.get("/internal/donate").query("donate_id", "abc").send().assertStatus(422);
            }

            @Test
            @DisplayName("환불된 후원은 조회되지 않는다")
            void refundedGone() {
                AppUser user = createUser();
                Long donateId = DonateFixtures.donate(user, DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);
                bean(DonateService.class).refundDonate(donateId, user.getId());

                client.get("/internal/donate").query("donate_id", donateId).send().assertStatus(404);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("ID가 없으면 DB의 모든 후원을 돌려준다")
            void all() {
                DonateFixtures.donate(createUser(), DonateFixtures.rewardOfNewProject(), PER_AMOUNT, 1L);

                DataCollectionResponse<DonateData> result = bean(DonateService.class).getAdminDonates(Optional.empty());

                assertEquals(bean(DonateRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("ID가 있으면 그 후원 하나")
            void one() {
                AppUser user = createUser();
                Reward reward = DonateFixtures.rewardOfNewProject();
                Long donateId = DonateFixtures.donate(user, reward, PER_AMOUNT, 1L);

                DataCollectionResponse<DonateData> result = bean(DonateService.class).getAdminDonates(Optional.of(donateId));

                assertEquals(1L, result.getAmount());
                DonateData data = result.getDatas().iterator().next();
                assertEquals(user.getId(), data.getUser().getId());
                assertEquals(reward.getId(), data.getReward().getId());
            }

            @Test
            @DisplayName("없는 ID이면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class,
                        () -> bean(DonateService.class).getAdminDonates(Optional.of(Long.MAX_VALUE)));
            }
        }
    }
}
