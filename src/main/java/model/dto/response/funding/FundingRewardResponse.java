package model.dto.response.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseData;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundingRewardResponse extends BaseData {
    private record UserAddressResponse(String address) {}
    private record RewardResponse(
            List<UserAddressResponse> userAddresses,
            Long maxQuantity,
            Long remainQuantity) {}

    private RewardResponse rewardResponse;
    private Long purposeAmount;
    private Long donateAmount;

}
