package repository;

import model.domain.AppUser;
import model.domain.Funding;
import model.domain.Review;

import java.util.Map;

public class ReviewRepository extends BaseRepositoryImpl<Review, Long>{
    public boolean isExistUserAndFunding(AppUser user, Funding funding ){
        return isExist(Filter.builder()
                .conditions(Map.of(
                        "funding", funding,
                        "user", user))
                .build()
        );
    }
}
