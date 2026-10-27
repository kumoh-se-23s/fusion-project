package repository;

import constant.FundingState;
import model.domain.Funding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRepository extends BaseRepositoryImpl<Funding, Long> {
    public Funding createFunding(Funding funding) {
        return create(funding);
    }
}
