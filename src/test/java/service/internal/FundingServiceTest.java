package service.internal;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("관리자 FundingService")
public class FundingServiceTest extends DatabaseTest {
    private FundingService fundingService;

    @BeforeEach
    void setUp() {
        fundingService = db.config.getAdminFundingService();
    }

    @Nested
    @DisplayName("updateFundingApprove")
    class UpdateFundingApprove {
        @Test
        @DisplayName("승인 대기 펀딩의 상태와 수정일을 변경한다")
        void success() {
        }

        @Test
        @DisplayName("승인 대기가 아니면 NotPendingFundingException")
        void notPending() {
        }

        @Test
        @DisplayName("없는 펀딩이면 NotFoundException")
        void notFound() {
        }
    }
}
