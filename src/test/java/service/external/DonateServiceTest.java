package service.external;

import example.db.DatabaseTest;
import exception.AlreadyDonatedException;
import exception.InvalidRequestException;
import exception.LimitNumberOutException;
import exception.NotFoundException;
import model.domain.AppUser;
import model.domain.Category;
import model.domain.Donate;
import model.domain.Funding;
import model.domain.Reward;
import model.dto.request.donate.CreateDonateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import repository.DonateRepository;
import repository.FundingRepository;
import repository.RewardRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DonateService")
public class DonateServiceTest extends DatabaseTest {

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

    @Nested
    @DisplayName("createDonate")
    public class CreateDonate {

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

        @Test
        @DisplayName("요청이 없으면 InvalidRequestException")
        void nullRequest() {

            Long beforeStock = currentStock();
            long beforeCount = donationCount();

            assertThrows(
                    InvalidRequestException.class,
                    () -> donateService.createDonate(null)
            );

            assertEquals(beforeStock, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @ParameterizedTest(name = "금액 {0}")
        @ValueSource(strings = {"0", "-1000"})
        @DisplayName("금액이 0 이하면 LimitNumberOutException")
        void invalidAmount(String amount) {

            CreateDonateRequest request =
                    new CreateDonateRequest(
                            user.getPk(),
                            reward.getPk(),
                            new BigDecimal(amount),
                            1L
                    );

            Long beforeStock = currentStock();
            long beforeCount = donationCount();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> donateService.createDonate(request)
            );

            assertEquals(beforeStock, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @ParameterizedTest(name = "수량 {0}")
        @ValueSource(longs = {0L, -1L})
        @DisplayName("수량이 0 이하면 LimitNumberOutException")
        void invalidQuantity(Long quantity) {

            CreateDonateRequest request =
                    new CreateDonateRequest(
                            user.getPk(),
                            reward.getPk(),
                            BigDecimal.valueOf(10000),
                            quantity
                    );

            Long beforeStock = currentStock();
            long beforeCount = donationCount();

            assertThrows(
                    LimitNumberOutException.class,
                    () -> donateService.createDonate(request)
            );

            assertEquals(beforeStock, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException")
        void userNotFound() {

            CreateDonateRequest request =
                    new CreateDonateRequest(
                            Long.MAX_VALUE,
                            reward.getPk(),
                            BigDecimal.valueOf(10000),
                            1L
                    );

            Long beforeStock = currentStock();
            long beforeCount = donationCount();

            assertThrows(
                    NotFoundException.class,
                    () -> donateService.createDonate(request)
            );

            assertEquals(beforeStock, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @Test
        @DisplayName("재고가 없으면 LimitNumberOutException")
        void soldOut() {

            // Given: 재고를 0으로 설정
            rewardRepository.update(
                    reward.getPk(),
                    java.util.Map.of("remainQuantity", 0L)
            );

            assertEquals(0L, currentStock());

            long beforeCount = donationCount();

            // When & Then
            assertThrows(
                    LimitNumberOutException.class,
                    () -> donateService.createDonate(validRequest())
            );

            assertEquals(0L, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @Test
        @DisplayName("남은 재고보다 많으면 LimitNumberOutException")
        void overQuantity() {

            // Given: 재고 2개
            rewardRepository.update(
                    reward.getPk(),
                    java.util.Map.of("remainQuantity", 2L)
            );

            CreateDonateRequest request =
                    new CreateDonateRequest(
                            user.getPk(),
                            reward.getPk(),
                            BigDecimal.valueOf(10000),
                            3L
                    );

            long beforeCount = donationCount();

            // When & Then
            assertThrows(
                    LimitNumberOutException.class,
                    () -> donateService.createDonate(request)
            );

            assertEquals(2L, currentStock());
            assertEquals(beforeCount, donationCount());
        }

        @Test
        @DisplayName("같은 펀딩에 이미 후원했으면 AlreadyDonatedException")
        void duplicate() {

            // Given: 첫 번째 후원 성공
            donateService.createDonate(validRequest());

            assertEquals(9L, currentStock());
            assertEquals(1L, donationCount());

            // When: 동일한 펀딩에 재후원
            CreateDonateRequest secondRequest = validRequest();

            // Then
            assertThrows(
                    AlreadyDonatedException.class,
                    () -> donateService.createDonate(secondRequest)
            );

            // 중복 후원으로 DB가 변경되면 안 됨
            assertEquals(9L, currentStock());
            assertEquals(1L, donationCount());
        }
    }
}