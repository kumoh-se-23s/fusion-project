package service.external;

import constant.ProjectState;
import example.db.DatabaseTest;
import model.domain.Project;
import model.dto.ProjectData;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import model.dto.response.project.ProjectRewardResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.user.UserAddressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProjectService")
public class ProjectServiceTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceTest.class);

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = db.config.getProjectService();
    }

    @Test
    @DisplayName("프로젝트의 리워드와 리워드별 후원자를 조회한다")
    void getProjectByReward() {
        ProjectRewardResponse response = projectService.getProjectByReward(db.maker1CompleteProject.getId());
        LOG.info(response.toJsonString());

        assertEquals(db.maker1CompleteProject.getId(), response.getProjectInfo().getProjectId());
        assertEquals(2, response.getRewards().size());
        Map<String, RewardInfoResponse> list = new HashMap<>();
        Map<String, UserAddressResponse> list2 = new HashMap<>();
        for (RewardUserResponse reward : response.getRewards()) {
            list.put(reward.getRewardInfoResponse().getName()
                    , reward.getRewardInfoResponse());
            for (UserAddressResponse user : reward.getUserAddressInfo()) {
                list2.put(user.getAddress(),
                        user);
            }
        }
        assertEquals(
                db.maker1CompleteReward1.getId(),
                list.get(db.maker1CompleteReward1.getName()).getRewardId());

        assertEquals(
                db.maker1CompleteReward2.getId(),
                list.get(db.maker1CompleteReward2.getName()).getRewardId());

        assertEquals(
                db.donateUser1.getId(),
                list2.get(db.donateUser1.getAddress()).getUserId());
    }
    @Test
    @DisplayName("프로젝트를 생성한다")
    void createProject() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MICROS);

        IdResponse response = projectService.createProject(
                db.maker1.getId(),
                CreateProjectRequest.builder()
                    .title("[시연 02] 테스트용 새 프로젝트")
                    .text("[시연 02] 프로젝트 소개")
                    .purposeAmount(new BigDecimal("123456"))
                    .endDate(endDate)
                    .categoryId(db.furniture.getId())
                    .build()
        );

        Project saved = db.config.getProjectRepository().findById(response.getId()).orElseThrow();
        assertEquals(db.maker1.getId(), saved.getMaker().getId());
        assertEquals("[시연 02] 테스트용 새 프로젝트", saved.getTitle());
        assertEquals("[시연 02] 프로젝트 소개", saved.getText());
        assertAmount("123456", saved.getPurposeAmount());
        assertEquals(endDate, saved.getEndDate());
        assertEquals(ProjectState.JUST_USER_INPUT, saved.getState());
        assertEquals(db.furniture.getId(), saved.getCategory().getId());
    }

    @Test
    @DisplayName("전체 프로젝트를 조회한다")
    void getAllProject() {
        DataCollectionResponse<ProjectData> response = projectService.getAllProject();
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
        DataCollectionResponse<ProjectData> response = projectService.getByCategoryAndState(
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
