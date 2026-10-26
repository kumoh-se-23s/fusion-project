package model.domain;

import annotation.Column;
import annotation.JoinColumn;
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
    @JoinColumn(name = "user_fk")
    private AppUser user;

    @NonNull
    @JoinColumn(name = "reward_fk")
    private Reward reward;

    @NonNull
    @Column(name = "donate_amount")
    private BigDecimal amount;

    @NonNull
    @Column(name = "donate_quantity")
    private Long quantity;

    @Builder.Default
    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "donate_state")
    private Boolean state = false;
}
