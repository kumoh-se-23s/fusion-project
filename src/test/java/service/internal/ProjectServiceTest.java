package service.internal;

import constant.ProjectState;
import example.db.DatabaseTest;
import exception.NotFoundException;
import model.domain.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("관리자 ProjectService")
public class ProjectServiceTest extends DatabaseTest {
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = db.config.getAdminProjectService();
    }

    @Test
    @DisplayName("승인 대기 프로젝트를 승인한다")
    void updateProjectApprove() {
        projectService.updateProjectApprove(db.changeStateProject.getId());

        Project saved = db.config.getProjectRepository().findById(db.changeStateProject.getId())
                .orElseThrow(NotFoundException::new);
        assertEquals(ProjectState.APPROVED, saved.getState());
        assertEquals(db.changeStateProject.getTitle(), saved.getTitle());
    }
}
