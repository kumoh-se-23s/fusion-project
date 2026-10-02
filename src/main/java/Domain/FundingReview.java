package Domain;

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
public class FundingReview {

    private Long reviewPk;
    private Funding funding;
    private AppUser user;
    private String reviewText;
    private BigDecimal reviewRating;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
