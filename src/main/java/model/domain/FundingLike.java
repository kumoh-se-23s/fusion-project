package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingLike implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private Funding funding;

    @NonNull
    private AppUser user;

    @NonNull
    private Boolean likeType;
}
