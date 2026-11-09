package repository;

import model.domain.DenyProject;
import model.domain.Project;

import java.util.List;
import java.util.Map;

public class DenyProjectRepository extends BaseRepositoryImpl<DenyProject, Long>{
    public List<DenyProject> findByProject(List<Project> projectList) {
        return find(Filter.builder()
                .conditions(Map.of("project", projectList))
                .build()
        );
    }
    public List<DenyProject> findByProject(Long projectId) {
        return find(Filter.builder()
                .conditions(Map.of("project", projectId))
                .build()
        );
    }
    public List<DenyProject> findByLimit(int limit) {
        return find(Filter.builder()
                .limit(limit)
                .build()
        );
    }
}
