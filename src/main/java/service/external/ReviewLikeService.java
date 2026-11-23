package service.external;

import exception.AlreadyExistException;
import exception.NotFoundException;
import exception.NotNullException;
import lombok.RequiredArgsConstructor;
import model.domain.AppUser;
import model.domain.Review;
import model.domain.ReviewLike;
import model.dto.request.like.LikeRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import repository.ReviewLikeRepository;
import repository.ReviewRepository;
import repository.UserRepository;
import service.BaseService;

import java.util.Objects;

/**
 * 리뷰의 좋아요를 관리하는 서비스 클래스
 * @since 2026-11-05 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ReviewLikeService extends BaseService {
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public IdResponse createReviewLike(Long ReviewId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            Review review = reviewRepository.findById(ReviewId)
                .orElseThrow(NotFoundException::new);

            AppUser user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

            updateLikeValue(review, request.getLikeType(), 1);

            return new IdResponse(
                    reviewLikeRepository.create(
                        ReviewLike.builder()
                            .likeType(request.getLikeType())
                            .review(review)
                            .user(user)
                            .build()
                    ).getId()

            );

        });
    }

    public IdResponse editReviewLike(Long ReviewId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(ReviewId, userId).stream()
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
            if (reviewLike.getLikeType() == request.getLikeType()) {
                throw new AlreadyExistException("동일한 좋아요 타입입니다");
            }
            Review review = updateLikeValue(reviewLike.getReview(), reviewLike.getLikeType(), -1);
            review = updateLikeValue(review, request.getLikeType(), 1);

            return new IdResponse(
                    reviewLikeRepository.update(review.getId(), request.toUpdateMap())
                            .orElseThrow(NotFoundException::new)
                            .getId()
            );
        });
    }
    public DeleteResponse deleteReviewLike(Long ReviewId, Long userId) {
        return tx.execute(() -> {
            ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(ReviewId, userId).stream()
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
            updateLikeValue(reviewLike.getReview(), reviewLike.getLikeType(), -1);
            return new DeleteResponse(reviewLikeRepository.delete(reviewLike.getId()));
        });
    }

    private Review updateLikeValue(Review review, Boolean likeType, int change) {
        if (Objects.isNull(likeType)) {
            throw new NotNullException("LikeType is Null");
        }

        Long current = likeType ? review.getLikeCount() : review.getDislikeCount();
        current += change;

        if (likeType) {
            return reviewRepository.updateByLike(review.getId(), Math.max(current, 0L))
                    .orElseThrow(NotFoundException::new);
        } else {
            return reviewRepository.updateByDislike(review.getId(), Math.max(current, 0L))
                    .orElseThrow(NotFoundException::new);
        }
    }
}
