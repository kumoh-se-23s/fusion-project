package service.internal;

import constant.ProjectState;
import exception.AlreadyExistException;
import exception.NotCompleteProjectException;
import exception.NotFoundException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.request.review.AdminEditReviewRequest;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.*;
import service.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 관리자가 리뷰를 관리하는 서비스 클래스
 * @since 2026-11-09
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class AdminReviewControlService extends BaseService {
    private static final BigDecimal MIN_RATING = new BigDecimal("0.0");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;

    public IdResponse createReview(CreateReviewRequest dto, Long userId) {
        return tx.execute(false, () -> {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(NotFoundException::new);

            Reward reward= rewardRepository.findById(dto.getRewardId())
                    .orElseThrow(NotFoundException::new);
// 관리자 api에서 굳이 검증은 필요없어 보임
//
//            List<Donate> donateList = donateRepository.findByUserAndReward(user, reward);
//
//            boolean isDelivered = false;
//            for (Donate donate : donateList) {
//                if (donate.getState()) {
//                    isDelivered = true;
//                    break;
//                }
//            }
//
//            if (reward.getProject().getState() != ProjectState.COMPLETED || !isDelivered) {
//                throw new NotCompleteProjectException("Not Allowed Write Review");
//            }
//
//            if (reviewRepository.isExistUserAndReward(user, reward)) {
//                throw new AlreadyExistException();
//            }


            Review review = Serializer.toDomain(dto, Review.class)
                    .toBuilder()
                    .reward(reward)
                    .user(user)
                    .build();

            BigDecimal rating = review.getRating();
            if (rating != null && (rating.compareTo(MIN_RATING) < 0 || rating.compareTo(MAX_RATING) > 0)) {
                throw new OutOfRangeException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
            }

            return new IdResponse(reviewRepository.create(review).getId());
        });
    }

    public IdResponse editReview(Long reviewId, AdminEditReviewRequest dto) {
        return tx.execute(false, () -> {

            Reward reward = rewardRepository.findById(dto.getRewardId())
                    .orElseThrow(NotFoundException::new);

            Map<String, Object> dtoMap = dto.getRequest().toUpdateMap();
            dtoMap.put("reward", reward);

            Review review = reviewRepository.update(reviewId, dtoMap)
                    .orElseThrow(NotFoundException::new);

            return new IdResponse(review.getId());
        });
    }

    public DeleteResponse deleteReview(Long reviewId) {
        return tx.execute(false, () ->{
            Review review =  reviewRepository.findById(reviewId)
                    .orElseThrow(NotFoundException::new);

            return new DeleteResponse(
                    reviewRepository.delete(reviewId)
            );
        });
    }

    public DeleteResponse deleteReviewAllLikes(Long reviewId) {
        return tx.execute(() -> {
            boolean result = true;

            List<ReviewLike> reviewLikeList = reviewLikeRepository.findByReview(reviewId);
            for (ReviewLike reviewLike : reviewLikeList) {
                if (!reviewLikeRepository.delete(reviewLike.getId())) {
                    result = false;
                } else {
                    updateLikeValue(reviewLike.getReview(), reviewLike.getLikeType(), -1);
                }
            }
            return new DeleteResponse(result);
        });
    }

    private void updateLikeValue(Review review, Boolean likeType, int change) {
        if (likeType) {
            reviewRepository.updateByLike(review.getId(), Math.max(review.getLikeCount() + change, 0));
        } else {
            reviewRepository.updateByDislike(review.getId(), Math.max(review.getDislikeCount() + change,0));
        }
    }
}
