package service.external;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DonateService")
class DonateServiceTest extends DatabaseTest {
    private DonateService donateService;

    @BeforeEach
    void setUp() {
        donateService = new DonateService(
                db.config.getDonateRepository(), db.config.getRewardRepository(),
                db.config.getFundingRepository(), db.config.getUserRepository());
    }

    @Nested
    @DisplayName("createDonate")
    class CreateDonate {
        @Test
        @DisplayName("후원을 생성한다")
        void success() {
        }

        @Test
        @DisplayName("요청이 없으면 InvalidRequestException")
        void nullRequest() {
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
        @DisplayName("없는 사용자면 NotFoundException")
        void userNotFound() {
        }

        @Test
        @DisplayName("재고가 없으면 LimitNumberOutException")
        void soldOut() {
        }

        @Test
        @DisplayName("남은 재고보다 많으면 LimitNumberOutException")
        void overQuantity() {
        }

        @Test
        @DisplayName("같은 펀딩에 이미 후원했으면 AlreadyDonatedException")
        void duplicate() {
        }
    }
}
