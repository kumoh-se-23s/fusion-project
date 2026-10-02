package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================================
// FUNDING_CATEGORY
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundingCategory {

    private Long fundingCategoryPk;
    private Category category;
    private Funding funding;
}
