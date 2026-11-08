package repository;

import model.domain.ProjectLike;

import java.util.List;
import java.util.Map;

public class ProjectLikeRepository extends BaseRepositoryImpl<ProjectLike, Long>{
    public List<ProjectLike> findByProjectAndUser(Long projectId, Long userId) {
        return find(Filter.builder()
                .conditions(Map.of("project", projectId, "user", userId))
                .build()
        );
    }
}
