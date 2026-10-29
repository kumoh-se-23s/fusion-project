package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "REVIEW_LIKE")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike extends BaseDomain {
    @PK
    @Column(name = "review_like_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "review_fk", nullable = false, uniqueGroup = "like")
    private Review review;

    @NonNull
    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "like")
    private AppUser user;

    @Builder.Default
    @NonNull
    @Column(name = "like_type", nullable = false)
    private Boolean likeType = true;
}
