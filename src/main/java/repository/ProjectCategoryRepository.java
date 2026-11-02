package repository;

import model.domain.Category;
import model.domain.Project;
import model.domain.ProjectCategory;

import java.util.List;
import java.util.Map;

public class ProjectCategoryRepository extends BaseRepositoryImpl<ProjectCategory, Long> {
    public List<ProjectCategory> findByProjectAndCategoryIds(List<Project> projects, List<Category> categories) {
        return find(Filter.builder()
                .conditions(Map.of(
                        "project", projects,
                        "category", categories))
                .build()
        );
    }
}
