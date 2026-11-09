package service.internal;

import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.DenyProject;
import model.domain.Review;
import model.dto.ReviewData;
import model.dto.ReviewData;
import model.dto.response.DataCollectionResponse;
import model.dto.response.SingleDataResponse;
import repository.ReviewRepository;
import service.BaseService;

import java.util.List;

/**
 * 관리자가 리뷰를 조회하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class AdminReviewQueryService extends BaseService {
    private ReviewRepository reviewRepository;

    public SingleDataResponse<ReviewData> getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(NotFoundException::new);

        return new SingleDataResponse<>(
            Serializer.toData(review, ReviewData.class)
        );
    }

    public DataCollectionResponse<ReviewData> getAllDenyProject(int limit) {
        List<Review> reviewList = reviewRepository.findByLimit(limit);

        return DataCollectionResponse.<ReviewData>builder()
                .datas(Serializer.toDataList(reviewList, ReviewData.class))
                .amount((long) reviewList.size())
                .build();
    }
}
