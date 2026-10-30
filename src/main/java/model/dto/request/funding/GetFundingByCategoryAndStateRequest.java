package model.dto.request.funding;

import constant.FundingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFundingByCategoryAndStateRequest extends Request {
    private List<String> categoryList;
    private List<FundingState> fundingStateList;
}
