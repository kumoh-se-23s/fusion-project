package model.dto.response.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.PKResponse;
import model.dto.response.Response;
import model.dto.response.fundingCategory.BindCategoryResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFundingByCategoryResponse extends Response {
    private PKResponse fundingPK;
    private BindCategoryResponse fundingCategoryPKs;
}
