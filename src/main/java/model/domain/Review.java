package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "REVIEW")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseDomain {
    @PK
    @Column(name = "review_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "funding_fk")
    private Funding funding;

    @NonNull
    @JoinColumn(name = "user_fk")
    private AppUser user;

    @Column(name = "review_text")
    private String text;

    @Builder.Default
    @NonNull
    @Column(name = "review_rating")
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Builder.Default
    @NonNull
    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Builder.Default
    @NonNull
    @Column(name = "dislike_count")
    private Long dislikeCount = 0L;

    @Builder.Default
    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "review_img_file")
    private byte[] imgFile;
}
