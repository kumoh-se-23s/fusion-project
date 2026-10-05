package model.domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike implements BaseDomain {

    @NonNull
    private Long pk;

    @NonNull
    private Review review;

    @NonNull
    private AppUser user;

    @NonNull
    private Boolean likeType;
}
