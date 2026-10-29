package service.external;

import constant.FundingState;
import exception.AlreadyExistException;
import exception.NotCompleteFundingException;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OutOfRangeException;
import lombok.RequiredArgsConstructor;
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
    private final FundingRepository fundingRepository;
    private final UserRepository userRepository;
    private final DonateRepository donateRepository;
    private final RewardRepository rewardRepository;
    //9. 리뷰 생성
    public PKResponse createReview(CreateReviewRequest dto, Long userPk) {
        return tx.execute(false, () -> {
            AppUser user = userRepository.findById(userPk)
                    .orElseThrow(NotFoundException::new);

            Funding funding = fundingRepository.findById(dto.getFundingPK())
                    .orElseThrow(NotFoundException::new);

            List<Reward> rewardList = rewardRepository.findByFunding(funding);
            List<Donate> donateList = donateRepository.findByUserAndRewardPK(user, rewardList);

            boolean isDelivered = false;
            for (Donate donate : donateList) {
                if (donate.getState()) {
                    isDelivered = true;
                    break;
                }
            }
            //완료되지 않은 펀딩이거나, 이 펀딩의 리워드를 배송받은 적이 없으면 오류 발생
            if (funding.getState() != FundingState.COMPLETED || !isDelivered) {
                throw new NotCompleteFundingException();
            }

            if (reviewRepository.isExistUserAndFunding(user, funding)) {
                throw new AlreadyExistException();
            }
            Review review;
            try {
                review = Review.builder()
                    .funding(funding)
                    .imgFile(dto.getImgFile())
                    .rating(dto.getRating())
                    .text(dto.getText())
                    .user(user)
                    .build();

            } catch (NullPointerException e) {
                throw new NotNullException("Review has Null Value");
            }
            //build()를 통과했으므로 rating은 null이 아님
            if (review.getRating().compareTo(MIN_RATING) < 0 || review.getRating().compareTo(MAX_RATING) > 0) {
                throw new OutOfRangeException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
            }
            return new PKResponse(reviewRepository.create(review).getPk());
        });
    }
}
