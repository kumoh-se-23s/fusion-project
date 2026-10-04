package Domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonateCancel implements BaseDomain {

    private Long pk;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private Reward reward;
    private AppUser user;
}
