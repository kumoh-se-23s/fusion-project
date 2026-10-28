package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "FUNDING_LIKE")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingLike extends BaseDomain {
    @PK
    @Column(name = "funding_like_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "funding_fk", nullable = false, uniqueGroup = "like")
    private Funding funding;

    @NonNull
    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "like")
    private AppUser user;

    @Builder.Default
    @NonNull
    @Column(name = "like_type", nullable = false)
    private Boolean likeType = true;
}
