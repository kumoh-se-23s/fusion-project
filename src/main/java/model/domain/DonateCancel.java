package model.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonateCancel implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private BigDecimal amount;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private Reward reward;

    @NonNull
    private AppUser user;
}
