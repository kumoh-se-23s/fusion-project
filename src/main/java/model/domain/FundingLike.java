package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingLike implements BaseDomain {

    private Long pk;
    private Funding funding;
    private AppUser user;
    private Boolean likeType;
}
