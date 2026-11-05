package model.dto.response.reward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.Response;
import model.dto.response.user.UserAddressResponse;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RewardUserResponse extends Response {
    private RewardInfoResponse rewardInfoResponse;
    private List<UserAddressResponse> userAddressInfo;

}
