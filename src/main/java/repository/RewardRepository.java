package repository;

import model.domain.Funding;
import model.domain.Reward;

import java.util.List;
import java.util.Map;

public class RewardRepository extends BaseRepositoryImpl<Reward, Long> {
    public List<Reward> findByFunding(Funding funding) {
        return find(
                    Filter.builder()
                            .conditions(Map.of("funding", funding))
                            .sort(Sort.by(Order.asc("name")))
                            .build()
        );
    }

}
