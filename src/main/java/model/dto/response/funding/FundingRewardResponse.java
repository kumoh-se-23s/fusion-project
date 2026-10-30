package model.dto.response.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;
import model.dto.response.reward.RewardUserResponse;

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
    private FundingInfoResponse fundingInfo;
    private List<RewardUserResponse> rewards;
}
