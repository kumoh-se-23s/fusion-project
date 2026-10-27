package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import repository.manager.Lazy;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "DONATE_CANCEL")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonateCancel extends BaseDomain {
    @PK
    @Column(name = "cancel_donate_pk")
    private Long pk;

    @NonNull
    @Column(name = "donate_amount", nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @NonNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NonNull
    @JoinColumn(name = "reward_fk", nullable = false)
    private Lazy<Reward> reward;

    @NonNull
    @JoinColumn(name = "user_fk", nullable = false)
    private Lazy<AppUser> user;
}
