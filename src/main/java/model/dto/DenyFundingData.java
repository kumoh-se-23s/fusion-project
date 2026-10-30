package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.DenyFunding;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = DenyFunding.class)
public class DenyFundingData extends BaseData {
    private Long pk;

    @DataField(name = "funding.pk")
    private Long fundingPK;

    @DataField(name = "reason")
    private String denyReason;
}
