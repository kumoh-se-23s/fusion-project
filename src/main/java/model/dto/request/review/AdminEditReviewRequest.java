package model.dto.request.review;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Review;
import model.dto.request.Request;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Review.class)
public class AdminEditReviewRequest extends Request {
    @DataField(name = "reward.id")
    private Long rewardId;

    private EditReviewRequest request;
}
