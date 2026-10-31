package repository;

import constant.FundingState;
import model.domain.Funding;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FundingRepository extends BaseRepositoryImpl<Funding, Long> {
    public Optional<Funding> updateByApprove(Long fundingPK) {
        return update(fundingPK, Map.of(
                "state", FundingState.APPROVED,
                "updatedAt", LocalDateTime.now()
        ));
    }

    public List<Funding> findByState(List<FundingState> stateList) {
        return find(Filter.builder()
                .conditions(Map.of("state", stateList))
                .build()
        );
    }
    public List<Funding> findAllOrderByCreatedAt() {
        return find(Filter.builder()
                .sort(Sort.by(Order.desc("createdAt")))
                .build()
        );
    }
    public List<Funding> findByUserPK(Long userPK) {
        return find(Filter.builder()
                .conditions(Map.of("maker", userPK))
                .build()
        );
    }
}
