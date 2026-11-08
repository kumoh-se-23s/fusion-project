package model.domain;

import annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//TODO reward DTO 카테고리 추가 필요
@Table(name = "REWARD")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reward extends BaseDomain {
    @Id
    @Column(name = "reward_id")
    private Long id;

    @JoinColumn(name = "project_fk", nullable = false, onDelete = OnDelete.CASCADE)
    private Project project;

    @Column(name = "reward_name", nullable = false)
    private String name;

    @Column(name = "reward_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "reward_img_file")
    private byte[] imgFile;

    @Positive
    @Builder.Default
    @Column(name = "required_amount", nullable = false)
    private BigDecimal requiredAmount = BigDecimal.ZERO;

    @Positive
    @Builder.Default
    @Column(name = "reward_max_quantity", nullable = false)
    private Long maxQuantity = 0L;

    @Positive
    @Builder.Default
    @Column(name = "reward_remain_quantity", nullable = false)
    private Long remainQuantity = 0L;

    @Column(name = "provide_date", nullable = false)
    private LocalDateTime provideDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
