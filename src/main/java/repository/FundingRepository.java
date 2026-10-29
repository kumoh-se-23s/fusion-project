package repository;

import constant.FundingState;
import model.domain.Funding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FundingRepository extends BaseRepositoryImpl<Funding, Long> {
    public Optional<Funding> updateByApprove(Long fundingPK) {
        return update(fundingPK, Map.of("state", FundingState.APPROVED));
    }

    public List<Funding> findByState(List<FundingState> stateList) {
        return find(Filter.builder()
                .conditions(Map.of("state", stateList))
                .build()
        );
    }

}
