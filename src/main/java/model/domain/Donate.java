package model.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donate implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private AppUser user;

    @NonNull
    private Reward reward;

    @NonNull
    private BigDecimal amount;

    @NonNull
    private Long quantity;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private Boolean state;
}
