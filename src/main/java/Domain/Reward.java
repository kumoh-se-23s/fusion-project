package Domain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    private Long rewardPk;
    private Funding funding;
    private String rewardName;
    private String rewardText;
    private String rewardImgUrl;
    private BigDecimal requiredAmount;
    private Long rewardMaxQuantity;
    private Long rewardRemainQuantity;
    private LocalDateTime serveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

