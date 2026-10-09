package model.dto.request.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateFundingRequest extends Request {
    private Long makerPK;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private BigDecimal donateAmount;

    private String text;

    private byte[] imgFile;

    private List<Long> categoryPKs;
}
