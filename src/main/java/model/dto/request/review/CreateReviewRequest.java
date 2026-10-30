package model.dto.request.review;

import annotation.BindDomain;
import annotation.DataField;
import lombok.*;
import model.domain.Review;
import model.dto.request.Request;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Review.class)
public class CreateReviewRequest extends Request {
    @DataField(name = "reward.pk")
    private Long rewardPK;

    private String text;

    private BigDecimal rating;

    private byte[] imgFile;
}
