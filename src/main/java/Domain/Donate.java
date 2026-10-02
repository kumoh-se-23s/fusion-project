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
public class Donate {

    private Long donatePk;
    private AppUser user;
    private Reward reward;
    private BigDecimal donateAmount;
    private Long donateQuantity;
    private LocalDateTime createdAt;
    private Boolean donateState;
}
