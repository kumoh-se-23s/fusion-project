package service.external;

import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.Reward;
import model.dto.ReviewData;
import model.dto.response.DataCollectionResponse;
import repository.ReviewRepository;
import repository.RewardRepository;
import service.BaseService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 일반유저가 리뷰를 조회하는 서비스 클래스
 * @since 2026-11-05 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ReviewQueryService extends BaseService {

    private final ReviewRepository reviewRepository;
    private final RewardRepository rewardRepository;

    public DataCollectionResponse<ReviewData> getUserReview(Long userId) {
        return tx.execute(true, () -> {
            List<ReviewData> dtoList = Serializer.toDataList(reviewRepository.findByUser(userId), ReviewData.class);
            return DataCollectionResponse.<ReviewData>builder()
                .datas(dtoList)
                .amount((long) dtoList.size())
                .build();
        });
    }
    public DataCollectionResponse<ReviewData> getRewardReview(Long rewardId) {
        return tx.execute(true, () -> {
            List<ReviewData> dtoList = Serializer.toDataList(reviewRepository.findByReward(rewardId), ReviewData.class);
            return DataCollectionResponse.<ReviewData>builder()
                .datas(dtoList)
                .amount((long) dtoList.size())
                .build();
        });
    }

    public DataCollectionResponse<ReviewData> getProjectReview(Long projectId) {
        return tx.execute(true, () -> {
            List<Reward> rewardList = rewardRepository.findByProjectId(projectId);
            List<ReviewData> dtoList = Serializer.toDataList(reviewRepository.findByRewards(rewardList), ReviewData.class);
            return DataCollectionResponse.<ReviewData>builder()
                .datas(dtoList)
                .amount((long) dtoList.size())
                .build();
        });
    }
}
