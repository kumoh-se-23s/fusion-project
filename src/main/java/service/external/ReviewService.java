package service.external;

import constant.FundingState;
import exception.AlreadyExistException;
import exception.NotCompleteFundingException;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
import model.Serializer;
import model.domain.*;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.PKResponse;
import repository.*;
import service.BaseService;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class ReviewService extends BaseService {
    private static final BigDecimal MIN_RATING = new BigDecimal("0.0");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;

    //9. 리뷰 생성
    public PKResponse createReview(CreateReviewRequest dto, Long userPk) {
        return tx.execute(false, () -> {
            AppUser user = userRepository.findByPK(userPk)
                    .orElseThrow(NotFoundException::new);

            Reward reward= rewardRepository.findByPK(dto.getRewardPK())
                    .orElseThrow(NotFoundException::new);

            List<Donate> donateList = donateRepository.findByUserAndReward(user, reward);

            boolean isDelivered = false;
            for (Donate donate : donateList) {
                if (donate.getState()) {
                    isDelivered = true;
                    break;
                }
            }

            if (reward.getFunding().getState() != FundingState.COMPLETED || !isDelivered) {
                throw new NotCompleteFundingException();
            }

            if (reviewRepository.isExistUserAndReward(user, reward)) {
                throw new AlreadyExistException();
            }

            Review review;
            try {
                review = Serializer.toDomain(dto, Review.class);
                review.toBuilder()
                        .reward(reward)
                        .build();

            } catch (NullPointerException e) {
                throw new NotNullException("Review has Null Value");
            }

            if (review.getRating().compareTo(MIN_RATING) < 0 || review.getRating().compareTo(MAX_RATING) > 0) {
                throw new OutOfRangeException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
            }

            return new PKResponse(reviewRepository.create(review).getPk());
        });
    }
}
