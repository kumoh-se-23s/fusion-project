import constant.ProjectState;
import example.db.DatabaseTest;
import example.db.InitDB;
import exception.LimitNumberOutException;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Review;
import model.domain.Reward;
import model.dto.ProjectData;
import model.dto.UserData;
import model.dto.request.donate.CreateDonateRequest;
import model.dto.request.project.CreateProjectRequest;
import model.dto.request.project.GetProjectByCategoryAndStateRequest;
import model.dto.request.review.CreateReviewRequest;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import model.dto.response.project.ProjectRewardResponse;
import model.dto.response.reward.RewardInfoResponse;
import model.dto.response.reward.RewardUserResponse;
import model.dto.response.user.UserAddressResponse;
import model.dto.response.user.UserResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import repository.Filter;
import service.UserService;
import service.external.ProjectService;
import service.external.ReviewService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("검사 항목")
@TestClassOrder(ClassOrderer.DisplayName.class)
public class MainTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(MainTest.class);

    //Main에서 실행하면 true가 되어, DB 초기화를 Main의 initDB() 호출에만 맡긴다
    static boolean skipInit = false;

    //모든 테이블을 삭제 후 다시 생성해 기본 데이터로 시작한다
    @BeforeAll
    static void initDB() {
        if (!skipInit) {
            InitDB.main(new String[0]);
        }
    }

    @Nested
    @DisplayName("01. MAKER - 프로젝트별 현황 보기")
    class Test01ViewProjectStatus {
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
    }

    @Nested
    @DisplayName("02. MAKER - 프로젝트 기본 정보 등록")
    class Test02RegisterProject {
        private ProjectService projectService;

        @BeforeEach
        void setUp() {
            projectService = db.config.getProjectService();
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
    }

    @Nested
    @DisplayName("03. MAKER - 리워드 등록 및 프로젝트 신청")
    class Test03RegisterReward {
        private service.external.RewardService rewardService;

        @BeforeEach
        void setup() {
            rewardService = db.config.getRewardService();
        }

        @Test
        @DisplayName("리워드를 등록한다")
        void createReward() {
            LocalDateTime provideDate = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MICROS);

            IdResponse response = rewardService.createReward(
                    CreateRewardDataRequest.builder()
                            .projectFk(db.maker1NoRewardProject.getId())
                            .rewardName("[시연 03] 테스트용 새 리워드")
                            .rewardText("[시연 03] 리워드 소개")
                            .provideDate(provideDate)
                            .requiredAmount(new BigDecimal("1000"))
                            .rewardMaxQuantity(10L)
                            .rewardRemainQuantity(5L)
                            .build()
            );
            Reward saved = db.config.getRewardRepository().findById(response.getId())
                    .orElseThrow();

            assertEquals("[시연 03] 테스트용 새 리워드", saved.getName());
            assertEquals("[시연 03] 리워드 소개", saved.getText());
            assertEquals(db.maker1NoRewardProject.getId(), saved.getProject().getId());
            assertEquals(ProjectState.PENDING_APPROVAL, saved.getProject().getState());
        }
    }

    @Nested
    @DisplayName("04. ADMIN - 프로젝트 등록 승인")
    class Test04ApproveProject {
        private service.internal.ProjectService projectService;

        @BeforeEach
        void setUp() {
            projectService = db.config.getAdminProjectService();
        }

        @Test
        @DisplayName("승인 대기 프로젝트를 승인한다")
        void updateProjectApprove() {
            projectService.updateProjectApprove(db.changeStateProject.getId());

            Project saved = db.config.getProjectRepository().findById(db.changeStateProject.getId()).orElseThrow();
            assertEquals(ProjectState.APPROVED, saved.getState());
            assertEquals(db.changeStateProject.getTitle(), saved.getTitle());

        }
    }

    @Nested
    @DisplayName("05. SUPPORTER - 개인정보 수정")
    class Test05UpdateProfile {
        private UserService userService;

        @BeforeEach
        void setUp() {
            userService = new UserService(
                    db.config.getUserRepository(),
                    db.config.getUserRoleRepository(),
                    db.config.getUserRoleRelationRepository()
            );
        }

        @Test
        @DisplayName("유저 정보를 업데이트한다.")
        void success() {
            AppUser maker = db.maker1;
            long makerId = maker.getId();


            UserResponse userResponse = userService.getMyInfo(makerId);
            UserData testUserData = userResponse.getUserData();

            testUserData.setName("[시연 05] 변경된 이름");


            IdResponse response = userService.updateUser(makerId, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findById(response.getId())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerId, response.getId());
            assertEquals(result.getId(), response.getId());
            assertEquals("[시연 05] 변경된 이름", result.getName());
        }
    }

    @Nested
    @DisplayName("06. SUPPORTER - 프로젝트 보기")
    class Test06ViewProjects {
        private ProjectService projectService;

        @BeforeEach
        void setUp() {
            projectService = db.config.getProjectService();
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
    }

    @Nested
    @DisplayName("07. SUPPORTER - 프로젝트 필터링")
    class Test07FilterProjects {
        private ProjectService projectService;

        @BeforeEach
        void setUp() {
            projectService = db.config.getProjectService();
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

    @Nested
    @DisplayName("08. SUPPORTER - 리워드 신청")
    class Test08ApplyReward {
        private service.external.DonateService donateService;

        @BeforeEach
        void setup() {
            donateService = db.config.getDonateService();
        }

        @Test
        @DisplayName("리워드를 신청한다")
        void createDonate() {
            IdResponse response = donateService.createDonate(
                    CreateDonateRequest.builder()
                            .quantity(2L)
                            .state(false)
                            .createdAt(LocalDateTime.now())
                            .rewardFk(db.maker1ApproveReward1.getId())
                            .userFk(db.notDonateUser1.getId())
                            .amount(new BigDecimal("2000"))
                            .build()
            );
            Donate saved = db.config.getDonateRepository().findById(response.getId())
                    .orElseThrow();

            assertEquals(false, saved.getState());
            assertEquals(db.maker1ApproveReward1.getId(), saved.getReward().getId());
            assertEquals(db.notDonateUser1.getId(), saved.getUser().getId());
            assertEquals(7L, saved.getReward().getRemainQuantity());
            assertEquals(saved.getAmount().add(db.maker1ApproveProject.getDonateAmount()), saved.getReward().getProject().getDonateAmount());
        }
    }

    @Nested
    @DisplayName("09. SUPPORTER - 리뷰 및 별점 등록")
    class Test09RegisterReview {
        private ReviewService reviewService;

        @BeforeEach
        void setUp() {
            reviewService = db.config.getReviewService();
        }

        @Test
        @DisplayName("배송 완료된 후원자가 리뷰를 작성한다")
        void createReview() {
            List<Review> list = db.config.getReviewRepository().find(
                    Filter.builder()
                            .conditions(Map.of("reward", db.maker1CompleteReward1.getId()))
                            .build()
            );
            for (Review review : list) {
                db.config.getReviewRepository().delete(review.getId());
            }

            IdResponse response = reviewService.createReview(CreateReviewRequest.builder()
                    .rewardId(db.maker1CompleteReward1.getId())
                    .text("[시연 09] 리뷰 생성 테스트 데이터")
                    .rating(new BigDecimal("1.0"))
                    .build(), db.donateUser1.getId());

            Review saved = db.config.getReviewRepository().findById(response.getId()).orElseThrow();
            assertEquals(db.maker1CompleteReward1.getId(), saved.getReward().getId());
            assertEquals(db.donateUser1.getId(), saved.getUser().getId());
            assertEquals("[시연 09] 리뷰 생성 테스트 데이터", saved.getText());
            assertAmount("1.0", saved.getRating());
        }
    }

    @Nested
    @DisplayName("10. SUPPORTER - 제공 가능 수량 동시성 제어")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Test10ConcurrencyControl {
        private service.external.DonateService donateService;

        @BeforeEach
        void setup() {
            donateService = db.config.getDonateService();
        }

        @Test
        @Order(1)
        @DisplayName("남은 수량만큼 신청하면 성공한다")
        void createDonate() {
            IdResponse response = donateService.createDonate(
                    CreateDonateRequest.builder()
                            .quantity(2L)
                            .state(false)
                            .createdAt(LocalDateTime.now())
                            .rewardFk(db.maker1ConcurrencyReward1.getId())
                            .userFk(db.notDonateUser2.getId())
                            .amount(new BigDecimal("2000"))
                            .build()
            );
            Donate saved = db.config.getDonateRepository().findById(response.getId())
                    .orElseThrow();

            assertEquals(false, saved.getState());
            assertEquals(db.maker1ConcurrencyReward1.getId(), saved.getReward().getId());
            assertEquals(db.notDonateUser2.getId(), saved.getUser().getId());
            assertEquals(0L, saved.getReward().getRemainQuantity());

        }

        //앞 테스트에서 수량을 모두 소진한 상태를 이어서 사용한다
        @Test
        @Order(2)
        @DisplayName("남은 수량을 넘겨서 신청하면 실패한다")
        void createDonateOverQuantity() {
            assertThrows(LimitNumberOutException.class,
                    () -> donateService.createDonate(
                            CreateDonateRequest.builder()
                                    .quantity(2L)
                                    .state(false)
                                    .createdAt(LocalDateTime.now())
                                    .rewardFk(db.maker1ConcurrencyReward1.getId())
                                    .userFk(db.notDonateUser3.getId())
                                    .build()
                    )
            );
        }
    }
}
