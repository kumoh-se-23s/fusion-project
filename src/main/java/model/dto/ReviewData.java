package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import lombok.*;
import model.domain.Review;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Review.class)
public class ReviewData extends BaseData {
    private Long pk;

    @DataField(name = "reward.pk")
    private Long rewardPK;

    @DataField(name = "user.pk")
    private Long userPK;

    private String text;

    private BigDecimal rating;

    private byte[] imgFile;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long likeCount;

    private Long dislikeCount;
}
