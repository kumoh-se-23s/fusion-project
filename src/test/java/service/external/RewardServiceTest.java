package service.external;

import example.db.DatabaseTest;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;
import model.dto.RewardData;
import model.dto.request.reward.CreateRewardDataRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import repository.CategoryRepository;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.RewardRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RewardService")
public class RewardServiceTest extends DatabaseTest {

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

    // =========================================================
    // 리워드 등록 테스트
    // =========================================================

    @Nested
    @DisplayName("createReward")
    public class CreateReward {

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

        @ParameterizedTest(name = "이름 \"{0}\"")
        @ValueSource(strings = {"", " "})
        @DisplayName("이름이 비어 있으면 LimitNumberOutException")
        void blankName(String name) {

            CreateRewardDataRequest request =
                    validRequest(funding.getPk(), name);

            int before =
                    rewardRepository.findByFundingPk(funding.getPk()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByFundingPk(funding.getPk()).size()
            );
        }

        @ParameterizedTest(name = "금액 {0}")
        @ValueSource(strings = {"0", "-1000"})
        @DisplayName("금액이 0 이하면 LimitNumberOutException")
        void invalidAmount(String amount) {

            CreateRewardDataRequest request =
                    new CreateRewardDataRequest(
                            "금액 테스트",
                            null,
                            LocalDateTime.now().plusDays(7),
                            "설명",
                            funding.getPk(),
                            category.getPk(),
                            new BigDecimal(amount),
                            10L,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

            int before =
                    rewardRepository.findByFundingPk(funding.getPk()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByFundingPk(funding.getPk()).size()
            );
        }

        @ParameterizedTest(name = "수량 {0}")
        @ValueSource(longs = {0L, -1L})
        @DisplayName("수량이 0 이하면 LimitNumberOutException")
        void invalidQuantity(Long quantity) {

            CreateRewardDataRequest request =
                    new CreateRewardDataRequest(
                            "수량 테스트",
                            null,
                            LocalDateTime.now().plusDays(7),
                            "설명",
                            funding.getPk(),
                            category.getPk(),
                            BigDecimal.valueOf(10000),
                            quantity,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

            int before =
                    rewardRepository.findByFundingPk(funding.getPk()).size();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> rewardService.createReward(request)
            );

            assertEquals(
                    before,
                    rewardRepository.findByFundingPk(funding.getPk()).size()
            );
        }

        @Test
        @DisplayName("없는 펀딩이면 NotFoundException")
        void fundingNotFound() {

            Long nonexistentPk = Long.MAX_VALUE;

            CreateRewardDataRequest request =
                    validRequest(nonexistentPk, "없는 펀딩 리워드");

            assertThrows(
                    NotFoundException.class,
                    () -> rewardService.createReward(request)
            );
        }
    }

    // =========================================================
    // 리워드 조회 테스트
    // =========================================================

    @Nested
    @DisplayName("리워드 조회")
    class GetReward {

        @Test
        @DisplayName("펀딩별 리워드를 조회한다")
        void byFunding() {

            // Given
            rewardService.createReward(
                    validRequest(funding.getPk(), "펀딩A 상품1")
            );

            rewardService.createReward(
                    validRequest(funding.getPk(), "펀딩A 상품2")
            );

            rewardService.createReward(
                    validRequest(secondFunding.getPk(), "펀딩B 상품")
            );

            // When
            List<RewardData> result =
                    rewardService.getRewardByFunding(funding.getPk());

            // Then
            assertEquals(2, result.size());

            assertTrue(
                    result.stream().allMatch(
                            reward -> funding.getPk().equals(reward.getFundingPk())
                    )
            );
        }

        @Test
        @DisplayName("후원한 리워드를 조회한다")
        void byDonate() {

            // Given
            rewardService.createReward(
                    validRequest(funding.getPk(), "후원한 리워드")
            );

            rewardService.createReward(
                    validRequest(secondFunding.getPk(), "후원 안 한 리워드")
            );

            Reward donatedReward =
                    findSavedReward(funding.getPk(), "후원한 리워드");

            Donate savedDonate = donateRepository.create(
                    Donate.builder()
                            .user(supporter)
                            .reward(donatedReward)
                            .amount(BigDecimal.valueOf(10000))
                            .quantity(1L)
                            .build()
            );

            assertNotNull(savedDonate);

            // When
            List<RewardData> result =
                    rewardService.getRewardByDonate(supporter.getPk());

            // Then
            assertEquals(1, result.size());

            assertEquals(
                    donatedReward.getPk(),
                    result.get(0).getRewardPk()
            );
        }

        @Test
        @DisplayName("메이커의 프로젝트별 리워드를 조회한다")
        void byMaker() {

            // Given
            rewardService.createReward(
                    validRequest(funding.getPk(), "첫 번째 프로젝트 리워드")
            );

            rewardService.createReward(
                    validRequest(secondFunding.getPk(), "두 번째 프로젝트 리워드")
            );

            // When
            List<List<RewardData>> result =
                    rewardService.getRewardFindByMaker(maker.getPk());

            // Then
            assertEquals(2, result.size());

            List<Long> allFundingPks = new ArrayList<>();

            for (List<RewardData> perProject : result) {
                for (RewardData reward : perProject) {
                    allFundingPks.add(reward.getFundingPk());
                }
            }

            assertTrue(allFundingPks.contains(funding.getPk()));
            assertTrue(allFundingPks.contains(secondFunding.getPk()));
        }
    }
}