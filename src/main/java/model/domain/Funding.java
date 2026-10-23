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
    @NonNull
    @PK
    @Column(name = "funding_pk")
    private Long pk;

    @NonNull
    @JoinColumn(name = "maker_fk")
    private AppUser maker;

    @NonNull
    @Column(name = "like_count")
    private Long likeCount;

    @NonNull
    @Column(name = "dislike_count")
    private Long dislikeCount;

    @NonNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NonNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NonNull
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @NonNull
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @NonNull
    @Column(name = "purpose_amount")
    private BigDecimal purposeAmount;

    @NonNull
    @Column(name = "donate_amount")
    private BigDecimal donateAmount;

    @NonNull
    @Column(name = "funding_state")
    private FundingState state;

    @Column(name = "funding_text")
    private String text;

    @Column(name = "funding_img_file")
    private Byte[] imgFile;
}
