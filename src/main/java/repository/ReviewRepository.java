package repository;

import model.domain.AppUser;
import model.domain.Funding;
import model.domain.Review;
import model.domain.Reward;

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
}
