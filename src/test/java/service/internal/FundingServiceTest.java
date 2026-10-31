package service.internal;

import constant.FundingState;
import example.db.DatabaseTest;
import model.domain.Funding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("관리자 FundingService")
public class FundingServiceTest extends DatabaseTest {
    private FundingService fundingService;

    @BeforeEach
    void setUp() {
        fundingService = db.config.getAdminFundingService();
    }

    @Test
    @DisplayName("승인 대기 펀딩을 승인한다")
    void updateFundingApprove() {
        fundingService.updateFundingApprove(db.changeStateFunding.getPk());

        Funding saved = db.config.getFundingRepository().findByPK(db.changeStateFunding.getPk()).orElseThrow();
        assertEquals(FundingState.APPROVED, saved.getState());
        assertEquals(db.changeStateFunding.getTitle(), saved.getTitle());
    }
}
