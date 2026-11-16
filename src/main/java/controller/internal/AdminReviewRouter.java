package controller.internal;

import auth.CurrentUser;
import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.ReviewData;
import model.dto.request.review.AdminReviewRequest;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminReviewControlService;
import service.internal.AdminReviewQueryService;

import java.util.Optional;

/**
 * 관리자 전용 프로젝트 거절 사유 라우터
 * @author 김민규
 * @since 2026-11-14
 * @version 1.0
 */
public class AdminReviewRouter extends BaseRouter {
    private final AdminReviewControlService adminReviewControlService;
    private final AdminReviewQueryService adminReviewQueryService;

    public AdminReviewRouter(
            AdminReviewControlService adminReviewControlService,
            AdminReviewQueryService adminReviewQueryService) {
        this.adminReviewControlService = adminReviewControlService;
        this.adminReviewQueryService = adminReviewQueryService;
        super("/internal/review");
    }
    @Post()
    public IdResponse createReview(AdminReviewRequest dto) {
        return adminReviewControlService.createReview(dto);
    }

    @Get("/review_id}")
    public SingleDataResponse<ReviewData> getReview(@Path("review_id") long reviewId) {
        return adminReviewQueryService.getReview(reviewId);

    }

    @Get()
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DataCollectionResponse<ReviewData> getAllReview(@Query("limit") Optional<Integer> limit) {
        return adminReviewQueryService.getAllReviews(limit);
    }

    @Patch("/review_id}")
    public IdResponse updateReview(@Path("review_id") long reviewId, AdminReviewRequest dto) {
        return adminReviewControlService.editReview(reviewId, dto);
    }

    @Delete("review_id}")
    public DeleteResponse deleteReview(@Path("review_id") long reviewId) {
        return adminReviewControlService.deleteReview(reviewId);
    }
}
