package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import constant.FundingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Funding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Funding.class)
public class FundingData extends BaseData {
    private Long pk;

    @DataField(name = "maker.pk")
    private Long makerPK;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long likeCount;

    private Long dislikeCount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private BigDecimal donateAmount;

    private FundingState state;

    private String title;

    private String text;

    private byte[] imgFile;
}
