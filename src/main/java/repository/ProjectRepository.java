package repository;

import constant.ProjectState;
import model.domain.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectRepository extends BaseRepositoryImpl<Project, Long> {
    public Optional<Project> updateByApprove(Long projectId) {
        return update(projectId, Map.of(
                "state", ProjectState.APPROVED,
                "updatedAt", LocalDateTime.now()
        ));
    }
    public Optional<Project> updateByDonateAmount(Long projectId, BigDecimal amount) {
        return update(projectId, Map.of(
                "donateAmount", amount,
                "updatedAt", LocalDateTime.now()
        ));
    }
    public Optional<Project> updateByPending(Long projectId) {
        return update(projectId, Map.of(
                "state", ProjectState.PENDING_APPROVAL,
                "updatedAt", LocalDateTime.now()
        ));
    }

    public List<Project> findByState(List<ProjectState> stateList) {
        return find(Filter.builder()
                .conditions(Map.of("state", stateList))
                .build()
        );
    }
    public List<Project> findAllOrderByCreatedAt() {
        return find(Filter.builder()
                .sort(Sort.by(Order.desc("createdAt")))
                .build()
        );
    }
    public List<Project> findByUserId(Long userId) {
        return find(Filter.builder()
                .conditions(Map.of("maker", userId))
                .build()
        );
    }
}
