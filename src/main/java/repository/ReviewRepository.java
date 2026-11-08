package repository;

import model.domain.AppUser;
import model.domain.Project;
import model.domain.Review;
import model.domain.Reward;

import java.util.List;
import java.util.Map;

public class ReviewRepository extends BaseRepositoryImpl<Review, Long>{
    public boolean isExistUserAndReward(AppUser user, Reward reward ){
        return isExist(Filter.builder()
                .conditions(Map.of(
                        "reward", reward,
                        "user", user))
                .build()
        );
    }
    public List<Review> findByUser(Long userId){
        return find(Filter.builder()
                .conditions(Map.of("user", userId))
                .build()
        );
    }
    public List<Review> findByReward(Long rewardId) {
        return find(Filter.builder()
                .conditions(Map.of("reward", rewardId))
                .build()
        );
    }
    public void updateByLike(Long reviewId, Long likeCount) {
        update(reviewId, Map.of("likeCount", likeCount));
    }
    public void updateByDislike(Long reviewId, Long dislikeCount) {
        update(reviewId, Map.of("dislikeCount", dislikeCount));
    }
}
