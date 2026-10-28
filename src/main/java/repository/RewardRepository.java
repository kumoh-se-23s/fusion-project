package repository;

import model.domain.Reward;

import java.util.List;
import java.util.Map;

public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {
    public List<Reward> findByFunding(Long fundingPK) {
        return find(
                    Filter.builder()
                            .conditions(Map.of("funding", fundingPK))
                            .sort(Sort.by(Order.asc("name")))
                            .build()
        );
    }

}
