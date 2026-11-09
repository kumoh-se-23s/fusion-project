package model.dto.response.reward;

import annotation.BindDomain;
import annotation.DataField;
import lombok.*;
import model.domain.Reward;
import model.dto.response.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = Reward.class)
public class RewardInfoResponse extends Response {
    @DataField(name = "id")
    private Long rewardId;

    private String name;
    private BigDecimal rewardPerAmount;
    private Long maxQuantity;
    private Long remainQuantity;
    private LocalDateTime provideDate;
}
