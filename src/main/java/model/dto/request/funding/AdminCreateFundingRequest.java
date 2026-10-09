package model.dto.request.funding;


import constant.FundingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreateFundingRequest extends Request {

    private CreateFundingRequest request;

    private FundingState state;

}