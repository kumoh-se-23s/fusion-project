package Domain;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingLike implements BaseDomain {

    private Long pk;
    private Funding funding;
    private AppUser user;
    private Boolean likeType;
}
