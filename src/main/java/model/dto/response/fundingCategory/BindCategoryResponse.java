package model.dto.response.fundingCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.PKResponse;
import model.dto.response.Response;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindCategoryResponse extends Response {
    private List<PKResponse> fundingCategoryPKs;
}
