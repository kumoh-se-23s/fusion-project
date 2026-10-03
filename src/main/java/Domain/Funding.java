package Domain;

import Domain.Enum.FundingState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Funding implements BaseDomain {

    private Long fundingPk;
    private AppUser maker;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal purposeAmount;
    private BigDecimal donateAmount;
    private FundingState fundingState;
    private String fundingText;
    private byte[] fundingImgFile;
}
