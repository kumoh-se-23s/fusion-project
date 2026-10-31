package service.external;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FundingService")
public class FundingServiceTest extends DatabaseTest {
    private FundingService fundingService;

    @BeforeEach
    void setUp() {
        fundingService = db.config.getFundingService();
    }

    @Nested
    @DisplayName("getFundingByReward")
    class GetFundingByReward {
        @Test
        @DisplayName("펀딩의 리워드와 리워드별 후원자를 조회한다")
        void success() {
        }

        @Test
        @DisplayName("없는 펀딩이면 NotFoundException")
        void notFound() {
        }
    }

    @Nested
    @DisplayName("createFunding")
    class CreateFunding {
        @Test
        @DisplayName("펀딩과 카테고리 연결을 생성한다")
        void success() {
        }

        @Test
        @DisplayName("없는 메이커면 NotFoundException")
        void makerNotFound() {
        }

        @Test
        @DisplayName("종료일이 없으면 NotNullException")
        void withoutEndDate() {
        }

        @Test
        @DisplayName("종료일이 시작일보다 빠르면 OutOfRangeException")
        void pastEndDate() {
        }

        @Test
        @DisplayName("목표 금액이 0 이하면 OutOfRangeException")
        void invalidPurposeAmount() {
        }
    }

    @Nested
    @DisplayName("getAllFunding")
    class GetAllFunding {
        @Test
        @DisplayName("전체 펀딩을 조회한다")
        void success() {
        }
    }

    @Nested
    @DisplayName("getByCategoryAndState")
    class GetByCategoryAndState {
        @Test
        @DisplayName("조건이 없으면 카테고리 없는 펀딩도 조회한다")
        void noCondition() {
        }

        @Test
        @DisplayName("상태로만 필터링한다")
        void byState() {
        }

        @Test
        @DisplayName("여러 카테고리에 속한 펀딩은 한 번만 조회한다")
        void withoutDuplicate() {
        }

        @Test
        @DisplayName("카테고리와 상태로 필터링한다")
        void byCategoryAndState() {
        }
    }
}
