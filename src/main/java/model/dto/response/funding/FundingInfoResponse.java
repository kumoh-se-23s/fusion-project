package model.dto.response.funding;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Funding;
import model.dto.response.Response;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = Funding.class)
public class FundingInfoResponse extends Response {

    @DataField(name = "pk")
    private Long fundingPK;

    private String title;

    private BigDecimal purposeAmount;

    private BigDecimal donateAmount;
}
