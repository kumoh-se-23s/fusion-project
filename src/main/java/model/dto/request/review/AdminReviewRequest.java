package model.dto.request.review;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Review;
import model.dto.request.Request;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminReviewRequest extends Request {
    @DataField(name = "reward.id")
    private Long rewardId;

    @DataField(name = "user.id")
    private Long userId;

    private ReviewDataRequest request;
}
