package Domain;

import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike implements BaseDomain {

    private Long pk;
    private Review review;
    private AppUser user;
    private Boolean likeType;
}
