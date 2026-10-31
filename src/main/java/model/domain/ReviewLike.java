package model.domain;

import annotation.*;
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

    @JoinColumn(name = "review_fk", nullable = false, uniqueGroup = "like", onDelete = OnDelete.CASCADE)
    private Review review;

    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "like", onDelete = OnDelete.CASCADE)
    private AppUser user;

    @Builder.Default
    @Column(name = "like_type", nullable = false)
    private Boolean likeType = true;
}
