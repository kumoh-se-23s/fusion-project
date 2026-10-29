package repository;

import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Reward;

import java.util.List;
import java.util.Map;

public class DonateRepository extends BaseRepositoryImpl<Donate, Long>{

    public List<Donate> findByRewards(List<Reward> rewards) {
        return find(Filter.builder()
                .conditions(Map.of("reward", rewards))
                .sort(Sort.by(Order.asc("pk")))
                .build()
        );
    }
    public List<Donate> findByUserAndRewardPK(AppUser user, List<Reward> rewards) {
        return find(Filter.builder()
                .conditions(Map.of(
                        "user", user,
                        "reward", rewards))
                .build()
        );

    }

}
