package model.dto.request.deny_funding;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.DenyFunding;
import model.dto.request.Request;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = DenyFunding.class)
public class DenyFundingRequest extends Request {
    private String denyReason;
}
