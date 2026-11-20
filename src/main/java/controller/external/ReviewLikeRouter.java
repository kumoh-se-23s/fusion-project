package controller.external;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.request.like.LikeRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import service.external.ReviewLikeService;

public class ReviewLikeRouter extends BaseRouter {
    private final ReviewLikeService reviewLikeService;

    public ReviewLikeRouter(ReviewLikeService reviewLikeService) {
        this.reviewLikeService = reviewLikeService;
        super("/reivew/like");
    }

    @Post("/{review_id}")
    public IdResponse createReviewLike(LikeRequest dto, @Path("review_id") Long reviewId, @Depends(CurrentUser.class) Long userId) {
        return reviewLikeService.createReviewLike(reviewId, userId, dto);
    }

    @Patch("/{review_id}")
    public IdResponse updateReviewLike(LikeRequest dto, @Path("review_id") Long reviewId, @Depends(CurrentUser.class) Long userId) {
        return reviewLikeService.editReviewLike(reviewId, userId, dto);
    }
    @Delete("/{review_id}")
    public DeleteResponse deleteReviewLike(@Path("review_id") Long reviewId, @Depends(CurrentUser.class) Long userId) {
        return reviewLikeService.deleteReviewLike(reviewId, userId);
    }


}
