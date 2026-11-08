package service.external;

import exception.NotFoundException;
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

/**
 * 리뷰의 좋아요를 관리하는 서비스 클래스
 * @since 2026-11-05 완성
 * @author 김민규
 * @version 1.0
**/
@RequiredArgsConstructor
public class ReviewLikeService extends BaseService {
    private final ReviewLikeRepository ReviewLikeRepository;
    private final ReviewRepository ReviewRepository;
    private final UserRepository userRepository;

    public IdResponse createReviewLike(Long ReviewId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            Review Review = ReviewRepository.findById(ReviewId)
                .orElseThrow(NotFoundException::new);

            AppUser user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

            updateLikeValue(Review, request.getLikeType(), 1);

            return new IdResponse(
                    ReviewLikeRepository.create(
                        ReviewLike.builder()
                            .likeType(request.getLikeType())
                            .review(Review)
                            .user(user)
                            .build()
                    ).getId()

            );

        });
    }

    public IdResponse editReviewLike(Long ReviewId, Long userId, LikeRequest request) {
        return tx.execute(() -> {
            ReviewLike ReviewLike = ReviewLikeRepository.findByReviewAndUser(ReviewId, userId).getFirst();

            updateLikeValue(ReviewLike.getReview(), request.getLikeType(), 1);

            return new IdResponse(
                    ReviewLikeRepository.update(ReviewLike.getId(), request.toUpdateMap())
                            .orElseThrow(NotFoundException::new)
                            .getId()
            );
        });
    }
    public DeleteResponse deleteReviewLike(Long ReviewId, Long userId) {
        return tx.execute(() -> {
            ReviewLike ReviewLike = ReviewLikeRepository.findByReviewAndUser(ReviewId, userId).getFirst();
            updateLikeValue(ReviewLike.getReview(), ReviewLike.getLikeType(), -1);
            return new DeleteResponse(ReviewLikeRepository.delete(ReviewLike.getId()));
        });
    }

    private void updateLikeValue(Review Review, Boolean likeType, int change) {
        if (likeType) {
            ReviewRepository.updateByLike(Review.getId(), Review.getLikeCount() + change);
        } else {
            ReviewRepository.updateByDislike(Review.getId(), Review.getDislikeCount() + change);
        }
    }
}
