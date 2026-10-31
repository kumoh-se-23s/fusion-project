package service.external;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("RewardService")
class RewardServiceTest extends DatabaseTest {
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(
                db.config.getFundingRepository(), db.config.getDonateRepository(),
                db.config.getRewardRepository(), db.config.getCategoryRepository());
    }

    @Nested
    @DisplayName("createReward")
    class CreateReward {
        @Test
        @DisplayName("남은 수량을 최대 수량과 같게 생성한다")
        void success() {
        }

        @ParameterizedTest(name = "이름 \"{0}\"")
        @ValueSource(strings = {"", " "})
        @DisplayName("이름이 비어 있으면 LimitNumberOutException")
        void blankName(String name) {
        }

        @ParameterizedTest(name = "금액 {0}")
        @ValueSource(strings = {"0", "-1000"})
        @DisplayName("금액이 0 이하면 LimitNumberOutException")
        void invalidAmount(String amount) {
        }

        @ParameterizedTest(name = "수량 {0}")
        @ValueSource(longs = {0L, -1L})
        @DisplayName("수량이 0 이하면 LimitNumberOutException")
        void invalidQuantity(Long quantity) {
        }

        @Test
        @DisplayName("없는 펀딩이면 NotFoundException")
        void fundingNotFound() {
        }
    }

    @Nested
    @DisplayName("리워드 조회")
    class GetReward {
        @Test
        @DisplayName("펀딩별 리워드를 조회한다")
        void byFunding() {
        }

        @Test
        @DisplayName("후원한 리워드를 조회한다")
        void byDonate() {
        }

        @Test
        @DisplayName("메이커의 프로젝트별 리워드를 조회한다")
        void byMaker() {
        }
    }
}
