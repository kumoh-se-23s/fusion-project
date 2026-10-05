package model.domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingCategory implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private Category category;

    @NonNull
    private Funding funding;
}
