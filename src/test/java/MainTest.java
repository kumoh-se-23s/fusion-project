import constant.FundingState;
import example.db.DatabaseTest;
import example.db.InitDB;
import exception.LimitNumberOutException;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.FundingCategory;
import model.domain.Review;
import model.domain.Reward;
import model.dto.FundingData;
import model.dto.UserData;
import model.dto.request.donate.CreateDonateRequest;
import model.dto.request.funding.CreateFundingByCategoryRequest;
import model.dto.request.funding.CreateFundingRequest;
import model.dto.request.funding.GetFundingByCategoryAndStateRequest;
import model.dto.request.review.CreateReviewRequest;
import model.dto.request.reward.CreateRewardDataRequest;
import model.dto.request.user.UpdateUserRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.PKResponse;
import model.dto.response.funding.CreateFundingByCategoryResponse;
import model.dto.response.funding.FundingRewardResponse;
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
import service.external.FundingService;
import service.external.ReviewService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @DisplayName("01. MAKER - 프로젝트별 펀딩 현황 보기")
    class Test01ViewFundingStatus {
        private FundingService fundingService;

        @BeforeEach
        void setUp() {
            fundingService = db.config.getFundingService();
        }

        @Test
        @DisplayName("펀딩의 리워드와 리워드별 후원자를 조회한다")
        void getFundingByReward() {
            FundingRewardResponse response = fundingService.getFundingByReward(db.maker1CompleteFunding.getPk());
            LOG.info(response.toJsonString());

            assertEquals(db.maker1CompleteFunding.getPk(), response.getFundingInfo().getFundingPK());
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
                    db.maker1CompleteReward1.getPk(),
                    list.get(db.maker1CompleteReward1.getName()).getRewardPk());

            assertEquals(
                    db.maker1CompleteReward2.getPk(),
                    list.get(db.maker1CompleteReward2.getName()).getRewardPk());

            assertEquals(
                    db.donateUser1.getPk(),
                    list2.get(db.donateUser1.getAddress()).getUserPK());
        }
    }

    @Nested
    @DisplayName("02. MAKER - 프로젝트 기본 정보 등록")
    class Test02RegisterProject {
        private FundingService fundingService;

        @BeforeEach
        void setUp() {
            fundingService = db.config.getFundingService();
        }

        @Test
        @DisplayName("펀딩을 생성한다")
        void createFunding() {
            LocalDateTime endDate = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MICROS);

            CreateFundingByCategoryResponse response = fundingService.createFunding(db.maker1.getPk(),
                    CreateFundingByCategoryRequest.builder()
                            .createFundingRequest(CreateFundingRequest.builder()
                                    .title("[시연 02] 테스트용 새 프로젝트")
                                    .text("[시연 02] 프로젝트 소개")
                                    .purposeAmount(new BigDecimal("123456"))
                                    .endDate(endDate)
                                    .build())
                            .categoryPKs(List.of(db.food.getPk(), db.tech.getPk()))
                            .build());

            Funding saved = db.config.getFundingRepository().findByPK(response.getPk()).orElseThrow();
            assertEquals(db.maker1.getPk(), saved.getMaker().getPk());
            assertEquals("[시연 02] 테스트용 새 프로젝트", saved.getTitle());
            assertEquals("[시연 02] 프로젝트 소개", saved.getText());
            assertAmount("123456", saved.getPurposeAmount());
            assertEquals(endDate, saved.getEndDate());
            assertEquals(FundingState.JUST_USER_INPUT, saved.getState());
            List<FundingCategory> list = db.config.getFundingCategoryRepository()
                    .find(Filter.builder()
                            .conditions(Map.of("funding", saved))
                            .build());

            assertEquals(saved.getPk(), list.getFirst().getFunding().getPk());
            assertEquals(Set.of(db.food.getPk(), db.tech.getPk()),
                    list.stream()
                            .map(fundingCategory -> fundingCategory.getCategory().getPk())
                            .collect(Collectors.toSet()));
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

            PKResponse response = rewardService.createReward(
                    CreateRewardDataRequest.builder()
                            .categoryFk(db.food.getPk())
                            .fundingFk(db.maker1NoRewardFunding.getPk())
                            .rewardName("[시연 03] 테스트용 새 리워드")
                            .rewardText("[시연 03] 리워드 소개")
                            .provideDate(provideDate)
                            .requiredAmount(new BigDecimal("1000"))
                            .rewardMaxQuantity(10L)
                            .rewardRemainQuantity(5L)
                            .build()
            );
            Reward saved = db.config.getRewardRepository().findByPK(response.getPk())
                    .orElseThrow();

            assertEquals("[시연 03] 테스트용 새 리워드", saved.getName());
            assertEquals("[시연 03] 리워드 소개", saved.getText());
            assertEquals(db.maker1NoRewardFunding.getPk(), saved.getFunding().getPk());
            assertEquals(FundingState.PENDING_APPROVAL, saved.getFunding().getState());
            assertEquals(db.food.getPk(), saved.getCategory().getPk());
        }
    }

    @Nested
    @DisplayName("04. ADMIN - 프로젝트 등록 승인")
    class Test04ApproveProject {
        private service.internal.FundingService fundingService;

        @BeforeEach
        void setUp() {
            fundingService = db.config.getAdminFundingService();
        }

        @Test
        @DisplayName("승인 대기 펀딩을 승인한다")
        void updateFundingApprove() {
            fundingService.updateFundingApprove(db.changeStateFunding.getPk());

            Funding saved = db.config.getFundingRepository().findByPK(db.changeStateFunding.getPk()).orElseThrow();
            assertEquals(FundingState.APPROVED, saved.getState());
            assertEquals(db.changeStateFunding.getTitle(), saved.getTitle());

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
            long makerPK = maker.getPk();


            UserResponse userResponse = userService.getMyInfo(makerPK);
            UserData testUserData = userResponse.getUserData();

            testUserData.setName("[시연 05] 변경된 이름");


            PKResponse response = userService.updateUser(makerPK, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findByPK(response.getPk())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerPK, response.getPk());
            assertEquals(result.getPk(), response.getPk());
            assertEquals("[시연 05] 변경된 이름", result.getName());
        }
    }

    @Nested
    @DisplayName("06. SUPPORTER - 프로젝트 보기")
    class Test06ViewProjects {
        private FundingService fundingService;

        @BeforeEach
        void setUp() {
            fundingService = db.config.getFundingService();
        }

        @Test
        @DisplayName("전체 펀딩을 조회한다")
        void getAllFunding() {
            DataCollectionResponse<FundingData> response = fundingService.getAllFunding();
            LOG.info(response.toJsonString());
            List<Long> pkList = response.getDatas().stream()
                    .map(FundingData::getPk).toList();
            assertTrue(pkList.contains(db.maker1ApproveFunding.getPk()));
            assertTrue(pkList.contains(db.maker2ApproveFunding.getPk()));
            assertTrue(pkList.contains(db.maker1PendingFunding.getPk()));
            assertTrue(pkList.contains(db.maker2PendingFunding.getPk()));
            assertTrue(pkList.contains(db.maker1CompleteFunding.getPk()));
        }
    }

    @Nested
    @DisplayName("07. SUPPORTER - 프로젝트 필터링")
    class Test07FilterProjects {
        private FundingService fundingService;

        @BeforeEach
        void setUp() {
            fundingService = db.config.getFundingService();
        }

        @Test
        @DisplayName("카테고리와 상태로 필터링한다")
        void getByCategoryAndState() {
            DataCollectionResponse<FundingData> response = fundingService.getByCategoryAndState(
                    GetFundingByCategoryAndStateRequest.builder()
                            .categoryList(List.of(db.tech.getName()))
                            .fundingStateList(List.of(FundingState.APPROVED))
                            .build());
            LOG.info(response.toJsonString());
            List<Long> pkList = response.getDatas().stream()
                    .map(FundingData::getPk).toList();
            assertTrue(pkList.contains(db.maker1ApproveFunding.getPk()));
            assertTrue(pkList.contains(db.maker2ApproveFunding.getPk()));

            response = fundingService.getByCategoryAndState(
                    GetFundingByCategoryAndStateRequest.builder()
                            .categoryList(List.of(db.furniture.getName()))
                            .fundingStateList(List.of(FundingState.APPROVED))
                            .build());
            LOG.info(response.toJsonString());
            pkList = response.getDatas().stream()
                    .map(FundingData::getPk).toList();
            assertTrue(pkList.contains(db.maker1ApproveFunding.getPk()));
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
            PKResponse response = donateService.createDonate(
                    CreateDonateRequest.builder()
                            .quantity(2L)
                            .state(false)
                            .createdAt(LocalDateTime.now())
                            .rewardFk(db.maker1ApproveReward1.getPk())
                            .userFk(db.notDonateUser1.getPk())
                            .build()
            );
            Donate saved = db.config.getDonateRepository().findByPK(response.getPk())
                    .orElseThrow();

            assertEquals(false, saved.getState());
            assertEquals(db.maker1ApproveReward1.getPk(), saved.getReward().getPk());
            assertEquals(db.notDonateUser1.getPk(), saved.getUser().getPk());
            assertEquals(7L, saved.getReward().getRemainQuantity());
            assertEquals(saved.getAmount().add(db.maker1ApproveFunding.getDonateAmount()), saved.getReward().getFunding().getDonateAmount());
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
                            .conditions(Map.of("reward", db.maker1CompleteReward1.getPk()))
                            .build()
            );
            for (Review review : list) {
                db.config.getReviewRepository().delete(review.getPk());
            }

            PKResponse response = reviewService.createReview(CreateReviewRequest.builder()
                    .rewardPK(db.maker1CompleteReward1.getPk())
                    .text("[시연 09] 리뷰 생성 테스트 데이터")
                    .rating(new BigDecimal("1.0"))
                    .build(), db.donateUser1.getPk());

            Review saved = db.config.getReviewRepository().findByPK(response.getPk()).orElseThrow();
            assertEquals(db.maker1CompleteReward1.getPk(), saved.getReward().getPk());
            assertEquals(db.donateUser1.getPk(), saved.getUser().getPk());
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
            PKResponse response = donateService.createDonate(
                    CreateDonateRequest.builder()
                            .quantity(2L)
                            .state(false)
                            .createdAt(LocalDateTime.now())
                            .rewardFk(db.maker1ConcurrencyReward1.getPk())
                            .userFk(db.notDonateUser2.getPk())
                            .build()
            );
            Donate saved = db.config.getDonateRepository().findByPK(response.getPk())
                    .orElseThrow();

            assertEquals(false, saved.getState());
            assertEquals(db.maker1ConcurrencyReward1.getPk(), saved.getReward().getPk());
            assertEquals(db.notDonateUser2.getPk(), saved.getUser().getPk());
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
                                    .rewardFk(db.maker1ConcurrencyReward1.getPk())
                                    .userFk(db.notDonateUser3.getPk())
                                    .build()
                    )
            );
        }
    }
}
