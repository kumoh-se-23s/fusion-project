package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.PK;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import constant.FundingState;

@Table(name = "FUNDING")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Funding extends BaseDomain {
    @PK
    @Column(name = "funding_pk")
    private Long pk;

    @JoinColumn(name = "maker_fk", nullable = false)
    private AppUser maker;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "dislike_count", nullable = false)
    private Long dislikeCount = 0L;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt  = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "purpose_amount", nullable = false)
    private BigDecimal purposeAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "donate_amount", nullable = false)
    private BigDecimal donateAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "funding_state", nullable = false, length = 30)
    private FundingState state = FundingState.JUST_USER_INPUT;

    @Column(name = "funding_title")
    private String title;

    @Column(name = "funding_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "funding_img_file")
    private byte[] imgFile;
}
