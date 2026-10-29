package repository;

import model.domain.Category;
import model.domain.Funding;
import model.domain.FundingCategory;

import java.util.List;
import java.util.Map;

public class FundingCategoryRepository extends BaseRepositoryImpl<FundingCategory, Long> {
    public List<FundingCategory> findByFundingAndCategoryPKs(List<Funding> fundings, List<Category> categories) {
        return find(Filter.builder()
                .conditions(Map.of(
                        "funding", fundings,
                        "category", categories))
                .build()
        );
    }
}
