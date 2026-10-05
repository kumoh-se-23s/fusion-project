package model.domain;

import constant.FundingState;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Funding implements BaseDomain {
    @NonNull
    private Long pk;

    @NonNull
    private AppUser maker;

    @NonNull
    private Long likeCount;

    @NonNull
    private Long dislikeCount;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private LocalDateTime updatedAt;

    @NonNull
    private LocalDateTime startDate;

    @NonNull
    private LocalDateTime endDate;

    @NonNull
    private BigDecimal purposeAmount;

    @NonNull
    private BigDecimal donateAmount;

    @NonNull
    private FundingState state;

    private String text;

    private byte[] imgFile;
}
