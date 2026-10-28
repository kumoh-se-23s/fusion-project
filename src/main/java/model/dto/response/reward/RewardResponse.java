package model.dto.response.reward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.donate.DonateUserAddressResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponse {
    private Long rewardPk;
    private String name;
    private BigDecimal requiredAmount;
    private Long maxQuantity;
    private Long remainQuantity;
    private LocalDateTime provideDate;
    private List<DonateUserAddressResponse> donateUserInfo;

}
