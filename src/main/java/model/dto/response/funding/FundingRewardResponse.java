package model.dto.response.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 펀딩의 리워드 목록과, 리워드별 후원자 배송 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundingRewardResponse extends Response {
    //후원 1건 = 배송 1건
    public record DonateUserAddressResponse(
            Long donatePk,
            Long userPk,
            String name,
            String phone,
            String postal,
            String address,
            Long quantity) {}

    public record RewardResponse(
            Long rewardPk,
            String name,
            BigDecimal requiredAmount,
            Long maxQuantity,
            Long remainQuantity,
            LocalDateTime provideDate,
            List<DonateUserAddressResponse> donors) {}

    private Long fundingPk;
    private BigDecimal purposeAmount;
    private BigDecimal donateAmount;
    private List<RewardResponse> rewards;
}
