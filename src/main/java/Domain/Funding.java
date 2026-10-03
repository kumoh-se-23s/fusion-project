package Domain;

import Domain.Enum.FundingState;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Funding implements BaseDomain {

    private Long pk;
    private AppUser maker;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal purposeAmount;
    private BigDecimal donateAmount;
    private FundingState state;
    private String text;
    private byte[] imgFile;
}
