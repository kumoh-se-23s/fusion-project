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
public class DonateCancel {

    private Long cancelDonate;
    private BigDecimal donateAmount;
    private LocalDateTime createdAt;
    private Reward reward;
    private AppUser user;
}
