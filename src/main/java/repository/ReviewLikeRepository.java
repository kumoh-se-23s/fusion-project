package repository;

import model.domain.ProjectLike;
import model.domain.ReviewLike;

import java.util.List;
import java.util.Map;

public class ReviewLikeRepository extends BaseRepositoryImpl<ReviewLike, Long>{
    public List<ReviewLike> findByReviewAndUser(Long reviewId, Long userId) {
        return find(Filter.builder()
                .conditions(Map.of("review", reviewId, "user", userId))
                .build()
        );
    }

    public List<ReviewLike> findByReview(Long reviewId) {
        return find(Filter.builder()
                .conditions(Map.of("review", reviewId))
                .build()
        );
    }
}
