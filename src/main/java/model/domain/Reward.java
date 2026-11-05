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

    @Builder.Default
    @Column(name = "required_amount", nullable = false)
    private BigDecimal requiredAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reward_max_quantity", nullable = false)
    private Long maxQuantity = 0L;

    @Builder.Default
    @Column(name = "reward_remain_quantity", nullable = false)
    private Long remainQuantity = 0L;

    @Column(name = "provide_date", nullable = false)//제공 날짜
    private LocalDateTime provideDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false)//리워드 생성 날짜
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)//수정날짜/시간
    private LocalDateTime updatedAt = LocalDateTime.now();
}
