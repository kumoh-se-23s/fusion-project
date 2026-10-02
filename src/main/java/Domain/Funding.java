package Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ============================================================
// FUNDING
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Funding {

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
    private String fundingState;
    private String fundingText;
    private byte[] fundingImgFile;
}
