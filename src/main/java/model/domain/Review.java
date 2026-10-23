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
    @NonNull
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

    @NonNull
    @Column(name = "review_rating")
    private BigDecimal rating;

    @NonNull
    @Column(name = "like_count")
    private Long likeCount;

    @NonNull
    @Column(name = "dislike_count")
    private Long dislikeCount;

    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NonNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
