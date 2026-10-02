package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike {

    private Long reviewLikePk;
    private FundingReview review;
    private AppUser user;
    private Boolean likeType;
}
