package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "REVIEW_LIKE")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike extends BaseDomain {
    @PK
    @Column(name = "review_like_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "review_fk")
    private Review review;

    @NonNull
    @JoinColumn(name = "user_fk")
    private AppUser user;

    @Builder.Default
    @NonNull
    @Column(name = "like_type")
    private Boolean likeType = true;
}
