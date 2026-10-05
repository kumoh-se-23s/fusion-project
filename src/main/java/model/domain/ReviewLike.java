package model.domain;

import lombok.*;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike implements BaseDomain {

    private Long pk;
    private Review review;
    private AppUser user;
    private Boolean likeType;
}
