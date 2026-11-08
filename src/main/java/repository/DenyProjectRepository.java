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
}
