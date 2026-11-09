package service.external;

import constant.ProjectState;
import exception.*;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.request.review.CreateReviewRequest;
import model.dto.request.review.EditReviewRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.*;
import service.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 일반유저가 리뷰를 관리하는 서비스 클래스
 * @since 2026-11-05
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ReviewCommandService extends BaseService {
    private static final BigDecimal MIN_RATING = new BigDecimal("0.0");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;

    //9. 리뷰 생성
    public IdResponse createReview(CreateReviewRequest dto, Long userId) {
        return tx.execute(false, () -> {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(NotFoundException::new);

            Reward reward= rewardRepository.findById(dto.getRewardId())
                    .orElseThrow(NotFoundException::new);

            List<Donate> donateList = donateRepository.findByUserAndReward(user, reward);

            boolean isDelivered = false;
            for (Donate donate : donateList) {
                if (donate.getState()) {
                    isDelivered = true;
                    break;
                }
            }

            if (reward.getProject().getState() != ProjectState.COMPLETED || !isDelivered) {
                throw new NotCompleteProjectException("Not Allowed Write Review");
            }

            if (reviewRepository.isExistUserAndReward(user, reward)) {
                throw new AlreadyExistException();
            }


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

    public IdResponse editReview(Long userId, Long reviewId, EditReviewRequest request) {
        return tx.execute(false, () -> {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(NotFoundException::new);

            if (!Objects.equals(review.getUser().getId(), userId)) {
                throw new InvalidUserException();
            }

            if (request.getRating().compareTo(MIN_RATING) < 0 || request.getRating().compareTo(MAX_RATING) > 0) {
                throw new OutOfRangeException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
            }

            Map<String, Object> dtoMap = request.toUpdateMap();

            return new IdResponse(
                    reviewRepository.update(reviewId, dtoMap).
                        orElseThrow(NotFoundException::new).getId()
            );
        });
    }

    public DeleteResponse deleteReview(Long userId, Long reviewId) {
        return tx.execute(false, () -> {
            Review review = reviewRepository.findById(reviewId)
                        .orElseThrow(NotFoundException::new);

            if (!Objects.equals(review.getUser().getId(), userId)) {
                throw new InvalidUserException();
            }

            return new DeleteResponse(
                    reviewRepository.delete(reviewId)
            );
        });
    }

}
