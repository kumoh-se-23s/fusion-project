package service;

import service.external.DonateService;
import exception.AlreadyDonatedException;
import model.domain.*;
import model.dto.request.donate.CreateDonateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DonateServiceTest {

    // 가짜 후원 저장소
    static class FakeDonateRepository extends DonateRepository {

        List<Donate> saved = new ArrayList<>();

        @Override
        public Donate create(Donate donate) {
            saved.add(donate);
            return donate;
        }

        @Override
        public boolean existsByUserPkAndFundingPk(Long userPk, Long fundingPk) {
            return saved.stream().anyMatch(d ->
                    d.getUser().getPk().equals(userPk)
                            && d.getReward().getFunding().getPk().equals(fundingPk)
            );
        }
    }

    @Test
    @DisplayName("8번 - 하나의 프로젝트에 한 번만 후원 가능")
    void duplicateDonateTest() {

        // Given: 사용자와 펀딩 생성
        AppUser user = AppUser.builder()
                .pk(1L)
                .name("홍길동")
                .email("hong@test.com")
                .build();

        Funding funding = Funding.builder()
                .pk(10L)
                .maker(user)
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        Category category = Category.builder()
                .pk(1L)
                .name("굿즈")
                .build();

        // 같은 프로젝트에 속한 리워드 2개
        Reward rewardA = Reward.builder()
                .pk(100L)
                .funding(funding)
                .category(category)
                .name("리워드 A")
                .remainQuantity(5L)
                .maxQuantity(5L)
                .provideDate(LocalDateTime.now().plusDays(7))
                .build();

        Reward rewardB = rewardA.toBuilder()
                .pk(101L)
                .name("리워드 B")
                .build();

        FakeDonateRepository donateRepository =
                new FakeDonateRepository();

        // 실제 DB 대신 가짜 조회 결과 제공
        UserRepository userRepository = new UserRepository() {
            @Override
            public Optional<AppUser> findByPK(Long pk) {
                return Optional.of(user);
            }
        };

        RewardRepository rewardRepository = new RewardRepository() {
            @Override
            public Optional<Reward> findByPK(Long pk) {
                if (pk.equals(100L)) return Optional.of(rewardA);
                if (pk.equals(101L)) return Optional.of(rewardB);
                return Optional.empty();
            }
        };

        FundingRepository fundingRepository = new FundingRepository() {
            @Override
            public Optional<Funding> findByPK(Long pk) {
                return Optional.of(funding);
            }
        };

        DonateService donateService = new DonateService(
                donateRepository,
                rewardRepository,
                fundingRepository,
                userRepository
        );

        // When: 첫 번째 리워드 신청
        assertDoesNotThrow(() ->
                donateService.createDonate(
                        new CreateDonateRequest(
                                1L, 100L,
                                BigDecimal.valueOf(10000), 1L
                        )
                )
        );

        // Then: 같은 프로젝트의 다른 리워드 신청은 차단
        assertThrows(
                AlreadyDonatedException.class,
                () -> donateService.createDonate(
                        new CreateDonateRequest(
                                1L, 101L,
                                BigDecimal.valueOf(10000), 1L
                        )
                )
        );

        // 후원은 한 건만 저장되어야 함
        assertEquals(1, donateRepository.saved.size());
    }


    @Test
    @DisplayName("10번 - 리워드 재고가 0개이면 후원 신청 불가")
    void zeroStockDonateTest() {

        // Given: 사용자 준비
        AppUser user = AppUser.builder()
                .pk(1L)
                .name("홍길동")
                .build();

        // 펀딩 준비
        Funding funding = Funding.builder()
                .pk(10L)
                .maker(user)
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        // 재고가 0개인 리워드
        Reward reward = Reward.builder()
                .pk(100L)
                .funding(funding)
                .category(Category.builder().pk(1L).name("굿즈").build())
                .name("테스트 리워드")
                .requiredAmount(BigDecimal.valueOf(10000))
                .maxQuantity(10L)
                .remainQuantity(0L)
                .provideDate(LocalDateTime.now().plusDays(7))
                .build();

        FakeDonateRepository donateRepository =
                new FakeDonateRepository();

        UserRepository userRepository = new UserRepository() {
            @Override
            public Optional<AppUser> findByPK(Long pk) {
                return Optional.of(user);
            }
        };

        RewardRepository rewardRepository = new RewardRepository() {
            @Override
            public Optional<Reward> findByPK(Long pk) {
                return Optional.of(reward);
            }
        };

        // 재고 검사에서 먼저 종료되므로 FundingRepository는 사용되지 않음
        DonateService donateService = new DonateService(
                donateRepository,
                rewardRepository,
                null,
                userRepository
        );

        CreateDonateRequest request = new CreateDonateRequest(
                1L,
                100L,
                BigDecimal.valueOf(10000),
                1L
        );

        // When & Then: 재고 부족 예외 발생
        assertThrows(
                LimitNumberOutException.class,
                () -> donateService.createDonate(request)
        );

        // 후원 내역이 저장되지 않았는지도 확인
        assertTrue(donateRepository.saved.isEmpty());
    }

}