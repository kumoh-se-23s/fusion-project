package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import repository.manager.Lazy;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "DONATE")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donate extends BaseDomain {
    @PK
    @Column(name = "donate_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "user_fk", nullable = false)
    private Lazy<AppUser> user;

    @NonNull
    @JoinColumn(name = "reward_fk", nullable = false)
    private Lazy<Reward> reward;

    @NonNull
    @Column(name = "donate_amount", nullable = false)
    private BigDecimal amount;

    @NonNull
    @Column(name = "donate_quantity", nullable = false)
    private Long quantity;

    @Builder.Default
    @NonNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "donate_state", nullable = false)
    private Boolean state = false;
}
