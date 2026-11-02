package service.external;

import constant.ProjectState;
import example.db.DatabaseTest;
import model.dto.ProjectData;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProjectQueryService")
public class ProjectQueryServiceTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectQueryServiceTest.class);

    private ProjectQueryService projectQueryService;

    @BeforeEach
    void setUp() {
        projectQueryService = db.config.getProjectQueryService();
    }

    @Test
    @DisplayName("전체 프로젝트를 조회한다")
    void getAllProject() {
        DataCollectionResponse<ProjectData> response = projectQueryService.getAllProject();
        LOG.info(response.toJsonString());
        List<Long> idList = response.getDatas().stream()
                .map(ProjectData::getId).toList();
        assertTrue(idList.contains(db.maker1ApproveProject.getId()));
        assertTrue(idList.contains(db.maker2ApproveProject.getId()));
        assertTrue(idList.contains(db.maker1PendingProject.getId()));
        assertTrue(idList.contains(db.maker2PendingProject.getId()));
        assertTrue(idList.contains(db.maker1CompleteProject.getId()));
    }

    @Test
    @DisplayName("카테고리와 상태로 필터링한다")
    void getByCategoryAndState() {
        DataCollectionResponse<ProjectData> response = projectQueryService.getByCategoryAndState(
                GetProjectByCategoryAndStateRequest.builder()
                        .categoryList(List.of(db.tech.getName()))
                        .projectStateList(List.of(ProjectState.APPROVED))
                        .build());
        LOG.info(response.toJsonString());
        List<Long> idList = response.getDatas().stream()
                .map(ProjectData::getId).toList();
        assertTrue(idList.contains(db.maker1ApproveProject.getId()));
        assertTrue(idList.contains(db.maker2ApproveProject.getId()));
    }
}
