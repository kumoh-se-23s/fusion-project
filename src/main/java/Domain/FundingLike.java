package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundingLike implements BaseDomain {

    private Long fundingLikePk;
    private Funding funding;
    private AppUser user;
    private Boolean likeType;
}
