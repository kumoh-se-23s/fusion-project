package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
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
    @PK
    @Column(name = "reward_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "funding_fk", nullable = false)
    private Funding funding;

    @NonNull
    @JoinColumn(name = "category_fk", nullable = false)
    private Category category;

    @NonNull
    @Column(name = "reward_name", nullable = false)
    private String name;

    @Column(name = "reward_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "reward_img_file")
    private byte[] imgFile;

    @Builder.Default
    @NonNull
    @Column(name = "required_amount", nullable = false)
    private BigDecimal requiredAmount = BigDecimal.ZERO;

    @Builder.Default
    @NonNull
    @Column(name = "reward_max_quantity", nullable = false)
    private Long maxQuantity = 0L;

    @Builder.Default
    @NonNull
    @Column(name = "reward_remain_quantity", nullable = false)
    private Long remainQuantity = 0L;

    @NonNull
    @Column(name = "provide_date", nullable = false)
    private LocalDateTime provideDate;

    @Builder.Default
    @NonNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
