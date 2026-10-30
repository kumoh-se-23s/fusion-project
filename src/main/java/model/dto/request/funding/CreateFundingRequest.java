package model.dto.request.funding;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Funding;
import model.dto.request.Request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Funding.class)
public class CreateFundingRequest extends Request {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private String title;

    private String text;

    private byte[] imgFile;
}
