package repository;

import constant.FundingState;
import model.domain.Funding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRepository extends BaseRepositoryImpl<Funding, Long> {
    public Funding createFunding(Funding funding) {
        return this.create(funding);
    }
    public List<Funding> findByState(FundingState state) {
        return this.find(new Filter(new HashMap<>(Map.of("state", state)), null, null));
    }
}
