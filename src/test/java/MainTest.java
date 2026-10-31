import constant.FundingState;
import example.db.DatabaseTest;
import example.db.InitDB;
import model.domain.AppUser;
import model.domain.Category;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.CategoryRepository;
import repository.DonateRepository;
import repository.Filter;
import repository.FundingRepository;
import repository.RewardRepository;
import repository.UserRepository;
import service.UserService;
import service.external.DonateService;
import service.external.FundingService;
import service.external.ReviewService;
import service.external.RewardService;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * 검사 항목별 단위 테스트. 각 서비스 테스트에서 메서드를 그대로 옮겨 왔다.
 * IDE에서 클래스 또는 항목 옆의 실행 버튼으로 실행하면 JUnit 결과 화면에 항목별로 표시된다.
 * main에서 항목의 주석을 해제해 실행하면 콘솔에 JUnit 요약이 출력된다.
 */
@DisplayName("검사 항목")
@TestClassOrder(ClassOrderer.DisplayName.class)
public class MainTest extends DatabaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(MainTest.class);

    public static void main(String[] args) {
        // initDB();                       // 검사에 필요한 사전 데이터를 insert하는 메소드 (모든 테이블을 삭제 후 다시 생성)
        // 검사할 항목 하나만 주석 해제하여 실행

        // test01ViewFundingStatus();      // MAKER     - 프로젝트별 펀딩 현황 보기
        // test02RegisterProject();        // MAKER     - 프로젝트 기본 정보 등록
        // test03RegisterReward();         // MAKER     - 리워드 등록 및 프로젝트 신청
        // test04ApproveProject();         // ADMIN     - 프로젝트 등록 승인
        // test05UpdateProfile();          // SUPPORTER - 개인정보 수정
        // test06ViewProjects();           // SUPPORTER - 프로젝트 보기
        // test07FilterProjects();         // SUPPORTER - 프로젝트 필터링
        // test08ApplyReward();            // SUPPORTER - 리워드 신청
        // test09RegisterReview();         // SUPPORTER - 리뷰 및 별점 등록
        // test10ConcurrencyControl();     // SUPPORTER - 제공 가능 수량 동시성 제어
    }

    private static void initDB() {
        InitDB.main(new String[0]);
    }

    private static void test01ViewFundingStatus() {
        run(Test01ViewFundingStatus.class);
    }

    private static void test02RegisterProject() {
        run(Test02RegisterProject.class);
    }

    private static void test03RegisterReward() {
        run(Test03RegisterReward.class);
    }

    private static void test04ApproveProject() {
        run(Test04ApproveProject.class);
    }

    private static void test05UpdateProfile() {
        run(Test05UpdateProfile.class);
    }

    private static void test06ViewProjects() {
        run(Test06ViewProjects.class);
    }

    private static void test07FilterProjects() {
        run(Test07FilterProjects.class);
    }

    private static void test08ApplyReward() {
        run(Test08ApplyReward.class);
    }

    private static void test09RegisterReview() {
        run(Test09RegisterReview.class);
    }

    //TODO 동시성 테스트를 작성하면 해당 클래스를 넘기도록 변경
    private static void test10ConcurrencyControl() {
        System.out.println("동시성 테스트가 아직 없습니다.");
    }

    private static void run(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(
                LauncherDiscoveryRequestBuilder.request().selectors(selectClass(testClass)).build(), listener);

        PrintWriter out = new PrintWriter(System.out, true);
        listener.getSummary().printTo(out);
        listener.getSummary().printFailuresTo(out);
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
                                    .title("테스트용 새 프로젝트")
                                    .text("프로젝트 소개")
                                    .purposeAmount(new BigDecimal("123456"))
                                    .endDate(endDate)
                                    .build())
                            .categoryPKs(List.of(db.food.getPk(), db.tech.getPk()))
                            .build());

            Funding saved = db.config.getFundingRepository().findByPK(response.getPk()).orElseThrow();
            assertEquals(db.maker1.getPk(), saved.getMaker().getPk());
            assertEquals("테스트용 새 프로젝트", saved.getTitle());
            assertEquals("프로젝트 소개", saved.getText());
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
        private RewardService rewardService;
        private UserRepository userRepository;
        private FundingRepository fundingRepository;
        private DonateRepository donateRepository;
        private RewardRepository rewardRepository;
        private CategoryRepository categoryRepository;

        private AppUser maker;
        private AppUser supporter;
        private Funding funding;
        private Funding secondFunding;
        private Category category;

        @BeforeEach
        void setUp() {

            userRepository = db.config.getUserRepository();
            fundingRepository = db.config.getFundingRepository();
            donateRepository = db.config.getDonateRepository();
            rewardRepository = db.config.getRewardRepository();
            categoryRepository = db.config.getCategoryRepository();

            rewardService = new RewardService(
                    fundingRepository,
                    donateRepository,
                    rewardRepository,
                    categoryRepository
            );

            // 테스트 데이터가 중복되지 않도록 고유값 생성
            String unique = UUID.randomUUID().toString().replace("-", "");

            maker = userRepository.create(
                    AppUser.builder()
                            .name("테스트 메이커")
                            .email("maker-" + unique + "@example.com")
                            .build()
            );

            supporter = userRepository.create(
                    AppUser.builder()
                            .name("테스트 서포터")
                            .email("supporter-" + unique + "@example.com")
                            .build()
            );

            assertNotNull(maker);
            assertNotNull(maker.getPk());
            assertNotNull(supporter);
            assertNotNull(supporter.getPk());

            category = categoryRepository.create(
                    Category.builder()
                            .name("테스트 카테고리 " + unique)
                            .build()
            );

            assertNotNull(category);
            assertNotNull(category.getPk());

            funding = createFunding();
            secondFunding = createFunding();
        }

        // 테스트용 펀딩 생성
        private Funding createFunding() {

            Funding saved = fundingRepository.create(
                    Funding.builder()
                            .maker(maker)
                            .endDate(LocalDateTime.now().plusDays(30))
                            .build()
            );

            assertNotNull(saved);
            assertNotNull(saved.getPk());

            return saved;
        }

        // 정상적인 리워드 등록 요청 생성
        private CreateRewardDataRequest validRequest(Long fundingPk, String name) {

            return new CreateRewardDataRequest(
                    name,
                    null,
                    LocalDateTime.now().plusDays(7),
                    "테스트 리워드 설명",
                    fundingPk,
                    category.getPk(),
                    BigDecimal.valueOf(10000),
                    10L,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        // DB에서 저장된 리워드 조회
        private Reward findSavedReward(Long fundingPk, String name) {

            return rewardRepository.findByFundingPk(fundingPk)
                    .stream()
                    .filter(reward -> name.equals(reward.getName()))
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError("리워드 조회 실패: " + name)
                    );
        }

        // 테스트 종료 후 생성 데이터 삭제
        @AfterEach
        void tearDown() {

            if (supporter != null && supporter.getPk() != null) {

                for (Donate donate :
                        donateRepository.findByUserPk(supporter.getPk())) {

                    if (donate.getReward() == null ||
                            donate.getReward().getFunding() == null) {
                        continue;
                    }

                    Long fundingPk = donate.getReward().getFunding().getPk();

                    if ((funding != null && fundingPk.equals(funding.getPk()))
                            || (secondFunding != null &&
                            fundingPk.equals(secondFunding.getPk()))) {

                        donateRepository.delete(donate.getPk());
                    }
                }
            }

            for (Funding f : new Funding[]{funding, secondFunding}) {

                if (f == null || f.getPk() == null) {
                    continue;
                }

                for (Reward reward : rewardRepository.findByFundingPk(f.getPk())) {
                    rewardRepository.delete(reward.getPk());
                }

                fundingRepository.delete(f.getPk());
            }

            if (category != null && category.getPk() != null) {
                categoryRepository.delete(category.getPk());
            }

            if (supporter != null && supporter.getPk() != null) {
                userRepository.delete(supporter.getPk());
            }

            if (maker != null && maker.getPk() != null) {
                userRepository.delete(maker.getPk());
            }
        }

        @Test
        @DisplayName("남은 수량을 최대 수량과 같게 생성한다")
        void success() {

            // Given
            CreateRewardDataRequest request =
                    validRequest(funding.getPk(), "정상 리워드");

            // When
            rewardService.createReward(request);

            // Then
            Reward saved =
                    findSavedReward(funding.getPk(), "정상 리워드");

            assertEquals(10L, saved.getMaxQuantity());
            assertEquals(10L, saved.getRemainQuantity());

            assertEquals(
                    0,
                    BigDecimal.valueOf(10000)
                            .compareTo(saved.getRequiredAmount())
            );

            assertEquals(funding.getPk(), saved.getFunding().getPk());
            assertEquals(category.getPk(), saved.getCategory().getPk());
        }
    }

    @Nested
    @DisplayName("04. ADMIN - 프로젝트 등록 승인")
    class Test04ApproveProject {
        //이름이 같은 external.FundingService와 구분하기 위해 전체 경로 사용
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

            testUserData.setName("호날두");


            PKResponse response = userService.updateUser(makerPK, new UpdateUserRequest(testUserData));
            AppUser result = db.config.getUserRepository().findByPK(response.getPk())
                    .orElseThrow(AssertionError::new);
            assertEquals(makerPK, response.getPk());
            assertEquals(result.getPk(), response.getPk());
            assertEquals("호날두", result.getName());
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
        }
    }

    @Nested
    @DisplayName("08. SUPPORTER - 리워드 신청")
    class Test08ApplyReward {
        private DonateService donateService;

        private DonateRepository donateRepository;
        private RewardRepository rewardRepository;

        private AppUser user;
        private AppUser maker;
        private Funding funding;
        private Reward reward;

        @BeforeEach
        void setUp() {

            donateRepository = db.config.getDonateRepository();
            rewardRepository = db.config.getRewardRepository();
            FundingRepository fundingRepository = db.config.getFundingRepository();
            UserRepository userRepository = db.config.getUserRepository();

            donateService = new DonateService(
                    donateRepository,
                    rewardRepository,
                    fundingRepository,
                    userRepository
            );

            String unique = UUID.randomUUID().toString();

            // 테스트 사용자 생성
            maker = userRepository.create(
                    AppUser.builder()
                            .name("테스트 메이커")
                            .email("maker-" + unique + "@example.com")
                            .build()
            );

            user = userRepository.create(
                    AppUser.builder()
                            .name("테스트 후원자")
                            .email("user-" + unique + "@example.com")
                            .build()
            );

            // 테스트 펀딩 생성
            funding = fundingRepository.create(
                    Funding.builder()
                            .maker(maker)
                            .endDate(LocalDateTime.now().plusDays(30))
                            .build()
            );

            // 테스트 카테고리 생성
            Category category = db.config.getCategoryRepository().create(
                    Category.builder()
                            .name("테스트 카테고리 " + unique)
                            .build()
            );

            // 초기 재고 10개
            reward = rewardRepository.create(
                    Reward.builder()
                            .funding(funding)
                            .category(category)
                            .name("테스트 리워드")
                            .requiredAmount(BigDecimal.valueOf(10000))
                            .maxQuantity(10L)
                            .remainQuantity(10L)
                            .provideDate(LocalDateTime.now().plusDays(7))
                            .build()
            );
        }

        // 정상 후원 요청 생성
        private CreateDonateRequest validRequest() {
            return new CreateDonateRequest(
                    user.getPk(),
                    reward.getPk(),
                    BigDecimal.valueOf(10000),
                    1L
            );
        }

        // DB에서 현재 재고 조회
        private Long currentStock() {
            return rewardRepository.findByPK(reward.getPk())
                    .orElseThrow()
                    .getRemainQuantity();
        }

        // DB에서 현재 사용자의 해당 펀딩 후원 건수 조회
        private long donationCount() {
            List<Donate> donations =
                    donateRepository.findByUserPk(user.getPk());

            return donations.stream()
                    .filter(d -> d.getReward() != null)
                    .filter(d -> d.getReward().getFunding() != null)
                    .filter(d -> funding.getPk().equals(
                            d.getReward().getFunding().getPk()
                    ))
                    .count();
        }

        @Test
        @DisplayName("후원을 생성한다")
        void success() {

            // Given
            CreateDonateRequest request = validRequest();

            Long beforeStock = currentStock();
            long beforeCount = donationCount();

            // When
            donateService.createDonate(request);

            // Then
            assertEquals(beforeCount + 1, donationCount());

            assertEquals(
                    beforeStock - 1,
                    currentStock()
            );

            assertTrue(
                    donateRepository.existsByUserPkAndFundingPk(
                            user.getPk(),
                            funding.getPk()
                    )
            );
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
            System.out.println(list.size());
            for (Review review : list) {
                db.config.getReviewRepository().delete(review.getPk());
            }

            PKResponse response = reviewService.createReview(CreateReviewRequest.builder()
                    .rewardPK(db.maker1CompleteReward1.getPk())
                    .text("리뷰 테스트 데이터")
                    .rating(new BigDecimal("1.0"))
                    .build(), db.donateUser1.getPk());

            Review saved = db.config.getReviewRepository().findByPK(response.getPk()).orElseThrow();
            assertEquals(db.maker1CompleteReward1.getPk(), saved.getReward().getPk());
            assertEquals(db.donateUser1.getPk(), saved.getUser().getPk());
            assertEquals("리뷰 테스트 데이터", saved.getText());
            assertAmount("1.0", saved.getRating());
        }
    }

    //TODO 10. SUPPORTER - 제공 가능 수량 동시성 제어: 동시성 테스트를 작성하면 여기에 추가
}
