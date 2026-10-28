package repository;

import model.domain.Donate;

import java.util.List;
import java.util.Map;

public class DonateRepository extends BaseRepositoryImpl<Donate, Long>{

    public List<Donate> findByRewards(List<Long> rewardPKs) {
        return find(Filter.builder()
                .conditions(Map.of("reward", rewardPKs))
                .sort(Sort.by(Order.asc("pk")))
                .build()
        );
     }
}
