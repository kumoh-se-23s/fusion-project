package service.internal;

import constant.ProjectState;
import example.db.DatabaseTest;
import model.domain.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ProjectCommandService")
public class ProjectMakerServiceTest extends DatabaseTest {
    private AdminProjectControlService adminProjectControlService;

    @BeforeEach
    void setUp() {
        adminProjectControlService = db.config.getAdminProjectControlService();
    }

    @Test
    @DisplayName("승인 대기 프로젝트를 승인한다")
    void updateProjectApprove() {
        adminProjectControlService.updateProjectApprove(db.changeStateProject.getId());

        Project saved = db.config.getProjectRepository().findById(db.changeStateProject.getId()).orElseThrow();
        assertEquals(ProjectState.APPROVED, saved.getState());
        assertEquals(db.changeStateProject.getTitle(), saved.getTitle());

    }
}
