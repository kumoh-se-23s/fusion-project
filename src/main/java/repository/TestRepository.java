package repository;

import model.domain.Funding;

public class TestRepository extends BaseRepositoryImpl<Funding, Long> {
    public Funding createFunding(Funding funding) {
        return save(funding);
    }
}

