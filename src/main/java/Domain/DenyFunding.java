package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// DENY_FUNDING
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DenyFunding {

    private Long denyFundingPk;
    private Funding funding;
    private String denyReason;
}
