package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import constant.FundingState;

@Table(name = "FUNDING")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Funding extends BaseDomain {
    @PK
    @Column(name = "funding_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "maker_fk")
    private AppUser maker;

    @Builder.Default
    @NonNull
    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Builder.Default
    @NonNull
    @Column(name = "dislike_count")
    private Long dislikeCount = 0L;

    @Builder.Default
    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt  = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @NonNull
    @Column(name = "start_date")
    private LocalDateTime startDate = LocalDateTime.now();

    @NonNull
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder.Default
    @NonNull
    @Column(name = "purpose_amount")
    private BigDecimal purposeAmount = BigDecimal.ZERO;

    @Builder.Default
    @NonNull
    @Column(name = "donate_amount")
    private BigDecimal donateAmount = BigDecimal.ZERO;

    @Builder.Default
    @NonNull
    @Column(name = "funding_state")
    private FundingState state = FundingState.PENDING_APPROVAL;

    @Column(name = "funding_text")
    private String text;

    @Column(name = "funding_img_file")
    private Byte[] imgFile;
}
