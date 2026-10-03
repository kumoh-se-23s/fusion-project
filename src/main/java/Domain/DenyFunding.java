package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding implements BaseDomain {
    private Long denyFundingPk;
    private Funding funding;
    private String denyReason;
}
