package controller.internal;

import com.minihttp.core.route.BaseRouter;
import com.minihttp.core.route.annotation.*;
import model.dto.ReviewData;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import model.dto.response.SingleDataResponse;
import service.internal.AdminReviewControlService;
import service.internal.AdminReviewQueryService;
/**
 * 관리자 전용 프로젝트 거절 사유 라우터
 * @author 김민규
 * @since 2026-11-14
 * @version 1.0
 */
public class AdminReviewProjectRouter extends BaseRouter {
    private final AdminReviewControlService adminReviewControlService;
    private final AdminReviewQueryService adminReviewQueryService;

    public AdminReviewProjectRouter(
            AdminReviewControlService adminReviewControlService,
            AdminReviewQueryService adminReviewQueryService) {
        this.adminReviewControlService = adminReviewControlService;
        this.adminReviewQueryService = adminReviewQueryService;
        super("/internal/project/deny");
    }

    @Post()
    public IdResponse createReview( CreateReviewRequest dto, long makerId) {
        return adminReviewControlService.createReview(dto, makerId);
    }

    @Get("/review_id}")
    public SingleDataResponse<ReviewData> getReview(@Path("review_id") long reviewId) {
        return adminReviewQueryService.getReview(reviewId);

    }

    @Get()
    public DataCollectionResponse<ReviewData> getAllReview(int limit) {
        return adminReviewQueryService.getAll(limit);
    }

    @Get("/{project_id}")
    public DataCollectionResponse<ReviewData> getAllReviewByProject(@Path("project_id") long projectId) {
        return adminReviewQueryService.getReviewByProject(projectId);
    }

    @Patch("/review_id}")
    public IdResponse updateReview(@Path(review_id") long reviewId, ReviewRequest dto) {
        return adminReviewControlService.editReview(reviewId, dto);
    }

    @Delete("review_id}")
    public DeleteResponse deleteReview(@Path(review_id") long reviewId) {
        return adminReviewControlService.deleteReview(reviewId);
    }
}
