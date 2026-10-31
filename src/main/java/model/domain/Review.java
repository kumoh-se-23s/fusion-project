package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "REVIEW")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseDomain {
    @PK
    @Column(name = "review_pk")
    private Long pk;

    @JoinColumn(name = "reward_fk", nullable = false, uniqueGroup = "review")
    private Reward reward;

    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "review")
    private AppUser user;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String text;

    @Builder.Default
    @Column(name = "review_rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "dislike_count", nullable = false)
    private Long dislikeCount = 0L;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "review_img_file")
    private byte[] imgFile;
}
