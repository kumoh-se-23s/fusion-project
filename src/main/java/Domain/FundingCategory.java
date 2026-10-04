package Domain;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FundingCategory implements BaseDomain {

    private Long pk;
    private Category category;
    private Funding funding;
}
