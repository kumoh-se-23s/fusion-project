package service.internal;

import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Reward;
import model.dto.ReviewData;
import model.dto.response.DataCollectionResponse;
import repository.ReviewRepository;
import repository.RewardRepository;
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
    private final ReviewRepository reviewRepository;
    private final RewardRepository rewardRepository;

    public DataCollectionResponse<ReviewData> getReview(Long reviewId) {
        return null;
    }

    public DataCollectionResponse<ReviewData> getAllReview(int limit) {
        return null;
    }
}
