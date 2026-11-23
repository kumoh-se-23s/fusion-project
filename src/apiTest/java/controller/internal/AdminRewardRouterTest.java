package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import fixture.DonateFixtures;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.RewardRepository;
import service.external.RewardService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminRewardRouter (/internal/reward)")
class AdminRewardRouterTest extends ApiTest {

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/reward/fetch-all")
    @DisplayName("GET /internal/reward/fetch-all - 관리자 전체 리워드 조회")
    class GetAllRewards {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 내가 만든 리워드가 들어 있는 전체 목록")
            void returnsAll() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                ApiResponse response = client.get("/internal/reward/fetch-all").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                JsonNode mine = StreamSupport.stream(datas.spliterator(), false)
                        .filter(node -> node.path("rewardId").asLong() == reward.getId())
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("방금 만든 리워드가 없다: " + response));
                assertEquals(reward.getName(), mine.path("name").asText());
                assertEquals(reward.getProject().getId(), mine.path("projectId").asLong());
                assertEquals(reward.getProject().getMaker().getId(), mine.path("makerId").asLong());
            }

            @Test
            @DisplayName("지운 리워드는 목록에서 빠진다")
            void deletedExcluded() {
                Reward reward = DonateFixtures.rewardOfNewProject();
                bean(RewardRepository.class).delete(reward.getId());

                ApiResponse response = client.get("/internal/reward/fetch-all").send().assertStatus(200);

                assertTrue(StreamSupport.stream(response.json("/datas").spliterator(), false)
                        .noneMatch(node -> node.path("rewardId").asLong() == reward.getId()));
            }

            @Test
            @DisplayName("허용하지 않은 메서드는 405")
            void wrongMethod() {
                client.post("/internal/reward/fetch-all").send().assertStatus(405);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB에 있는 리워드 수와 amount가 같다")
            void amountMatchesRows() {
                DonateFixtures.rewardOfNewProject();

                DataCollectionResponse<RewardData> result = bean(RewardService.class).getAllRewards();

                assertEquals(bean(RewardRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("리워드 필드가 DTO로 옮겨진다")
            void mapsFields() {
                Reward reward = DonateFixtures.rewardOfNewProject();

                RewardData data = bean(RewardService.class).getAllRewards().getDatas().stream()
                        .filter(d -> d.getRewardId().equals(reward.getId()))
                        .findFirst()
                        .orElseThrow();

                assertEquals(reward.getName(), data.getName());
                assertEquals(reward.getText(), data.getText());
                assertEquals(0, reward.getRewardPerAmount().compareTo(data.getRewardPerAmount()));
                assertEquals(reward.getMaxQuantity(), data.getMaxQuantity());
                assertEquals(reward.getRemainQuantity(), data.getRemainQuantity());
            }
        }
    }
}
