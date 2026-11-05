package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "DONATE_CANCEL")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonateCancel extends BaseDomain {
    @Id
    @Column(name = "cancel_donate_id")
    private Long id;

    @Column(name = "donate_amount", nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @JoinColumn(name = "reward_fk", nullable = false)
    private Reward reward;

    @JoinColumn(name = "user_fk", onDelete = OnDelete.SET_NULL)
    private AppUser user;
}
