package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.ReviewData;
import model.dto.request.review.CreateReviewRequest;
import model.dto.request.review.ReviewDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.external.ReviewCommandService;
import service.external.ReviewQueryService;

/**
 * 관리자 전용 프로젝트 거절 사유 라우터
 * @author 김민규
 * @since 2026-11-14
 * @version 1.0
 */
public class ReviewRouter extends BaseRouter {
    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    public ReviewRouter(
            ReviewCommandService reviewCommandService,
            ReviewQueryService reviewQueryService) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
        super("/review");
    }
    @Post()
    public IdResponse createReview(CreateReviewRequest dto, @Depends(CurrentUser.class) Long userId) {
        return reviewCommandService.createReview(dto, userId);
    }

    @Get("/{review_id}")
    public SingleDataResponse<ReviewData> getReview(@Path("review_id") long reviewId) {
        return reviewQueryService.getReview(reviewId);
    }

    @Get("/my")
    public DataCollectionResponse<ReviewData> getMyReview(@Depends(CurrentUser.class) Long userId) {
        return reviewQueryService.getUserReview(userId);
    }
    @Get("/reward/{reward_id}")
    public DataCollectionResponse<ReviewData> getRewardReview(@Path("reward_id") Long rewardId) {
        return reviewQueryService.getRewardReview(rewardId);
    }

    @Get("/project/{project_id}")
    public DataCollectionResponse<ReviewData> getProjectReview(@Path("project_id") Long projectId) {
        return reviewQueryService.getProjectReview(projectId);
    }
    @Patch("/{review_id}")
    public IdResponse updateReview(
            @Path("review_id") Long reviewId,
            @Depends(CurrentUser.class) Long userId,
            ReviewDataRequest dto) {
        return reviewCommandService.editReview(userId, reviewId, dto);
    }

    @Delete("/{review_id}")
    public DeleteResponse deleteReview(@Depends(CurrentUser.class) Long userId, @Path("review_id") Long reviewId) {
        return reviewCommandService.deleteReview(userId, reviewId);
    }
}
