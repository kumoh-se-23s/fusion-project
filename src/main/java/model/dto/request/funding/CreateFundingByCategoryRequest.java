package model.dto.request.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFundingByCategoryRequest extends Request {
    private CreateFundingRequest createFundingRequest;
    private List<Long> categoryPKs;
}
