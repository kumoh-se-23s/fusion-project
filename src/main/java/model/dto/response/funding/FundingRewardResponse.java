package model.dto.response.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;
import model.dto.response.reward.RewardResponse;

import java.math.BigDecimal;
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
    private Long pk;
    private String title;
    private BigDecimal purposeAmount;
    private BigDecimal donateAmount;
    private List<RewardResponse> rewards;
}
