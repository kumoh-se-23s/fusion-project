package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "REWARD")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reward extends BaseDomain {
    @NonNull
    @PK
    @Column(name = "reward_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "funding_fk")
    private Funding funding;

    @NonNull
    @JoinColumn(name = "category_fk")
    private Category category;

    @NonNull
    @Column(name = "reward_name")
    private String name;

    @Column(name = "reward_text")
    private String text;

    @Column(name = "reward_img_file")
    private Byte[] imgFile;

    @NonNull
    @Column(name = "required_amount")
    private BigDecimal requiredAmount;

    @NonNull
    @Column(name = "reward_max_quantity")
    private Long maxQuantity;

    @NonNull
    @Column(name = "reward_remain_quantity")
    private Long remainQuantity;

    @NonNull
    @Column(name = "provide_date")
    private LocalDateTime provideDate;

    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NonNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
