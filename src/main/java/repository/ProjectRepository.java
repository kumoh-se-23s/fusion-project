package repository;

import constant.ProjectState;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.DenyProject;
import model.domain.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    public Optional<Project> updateByLike(Long projectId, Long likeCount) {
        return update(projectId, Map.of("likeCount", likeCount));
    }
    public Optional<Project> updateByDislike(Long projectId, Long dislikeCount) {
        return update(projectId, Map.of("dislikeCount", dislikeCount));
    }
    public List<Project> findByStateAndCategory(List<ProjectState> stateList, List<Category> categoryList) {
        Map<String, Object> filter = new HashMap<>();
        if (stateList != null) filter.put("state", stateList);

        if (categoryList != null) filter.put("category", categoryList);

        return find(Filter.builder()
                .conditions(filter)
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
    public List<Project> findByFilter(
            String title,
            AppUser maker,
            Category category,
            List<ProjectState> stateList,
            int limit,
            Sort sort
    ) {
        Filter filter = Filter.builder()
                .sort(sort)
                .limit(limit)
                .build();

        if (title != null) {
            filter.and("title", title);
        }
        if (category != null) {
            filter.and("category", category);
        }
        if (!stateList.isEmpty()) {
            filter.and("state", stateList);
        }
        if (maker != null) {
            filter.and("maker", maker);
        }

        return find(filter);
    }

    public List<Project> findByPending(int limit) {
        return find(Filter.builder()
                .conditions(Map.of("state", ProjectState.PENDING_APPROVAL))
                .limit(limit)
                .build()
        );
    }
}
