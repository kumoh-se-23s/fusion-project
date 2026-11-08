package model.domain;

import annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "DONATE")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donate extends BaseDomain {
    @Id
    @Column(name = "donate_id")
    private Long id;

    @JoinColumn(name = "user_fk", onDelete = OnDelete.SET_NULL)
    private AppUser user;

    @JoinColumn(name = "reward_fk", nullable = false)
    private Reward reward;

    @Positive
    @Column(name = "donate_amount", nullable = false)
    private BigDecimal amount;

    @Positive
    @Column(name = "donate_quantity", nullable = false)
    private Long quantity;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //배송 완료 여부 (true: 배송 완료)
    @Builder.Default
    @Column(name = "delivery_state", nullable = false)
    private Boolean state = false;
}
