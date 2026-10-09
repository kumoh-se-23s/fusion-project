package model.dto.request.deny_funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DenyFundingRequest extends Request {
    private String denyReason;
}
