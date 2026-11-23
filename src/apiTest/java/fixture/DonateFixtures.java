package fixture;

import constant.ProjectState;
import model.domain.AppUser;
import model.domain.Donate;
import model.domain.Project;
import model.domain.Reward;
import model.dto.request.donate.CreateDonateRequest;
import repository.DonateRepository;
import repository.ProjectRepository;
import repository.RewardRepository;
import repository.UserRepository;
import service.external.DonateService;
import support.TestServer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 후원/리워드 테스트용 데이터.
 * 후원은 (메이커 -> 프로젝트 -> 리워드) 줄기가 있어야 만들 수 있으므로 그 줄기를 한 번에 만든다.
 */
public final class DonateFixtures {
    /** 리워드 기본 단가 */
    public static final BigDecimal PER_AMOUNT = new BigDecimal("10000");
    /** 리워드 기본 수량 */
    public static final long MAX_QUANTITY = 10L;

    private DonateFixtures() {
    }

    private static <T> T bean(Class<T> type) {
        return TestServer.get().bean(type);
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 필수 값만 채운 유저 */
    public static AppUser user() {
        String key = unique("u");
        return bean(UserRepository.class).create(AppUser.builder()
                .name("테스트 " + key)
                .nickname(key)
                .email(key + "@test.com")
                .build());
    }

    /** 진행 중인 프로젝트 */
    public static Project project(AppUser maker) {
        return project(maker, ProjectState.IN_PROGRESS);
    }

    /** 상태를 지정한 프로젝트. 누적 후원 금액은 0 */
    public static Project project(AppUser maker, ProjectState state) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .title(unique("project"))
                .text("후원 테스트 프로젝트")
                .state(state)
                .purposeAmount(new BigDecimal("1000000"))
                .endDate(LocalDateTime.now().plusDays(30))
                .build());
    }

    /** 단가 10000원, 수량 10개인 리워드 */
    public static Reward reward(Project project) {
        return reward(project, MAX_QUANTITY);
    }

    /** 수량을 지정한 리워드 (남은 수량 = 최대 수량) */
    public static Reward reward(Project project, long quantity) {
        return bean(RewardRepository.class).create(Reward.builder()
                .project(project)
                .name(unique("reward"))
                .text("후원 테스트 리워드")
                .rewardPerAmount(PER_AMOUNT)
                .maxQuantity(quantity)
                .remainQuantity(quantity)
                .provideDate(LocalDateTime.now().plusDays(60))
                .build());
    }

    /** 새 메이커의 진행 중 프로젝트에 딸린 리워드 */
    public static Reward rewardOfNewProject() {
        return reward(project(user()));
    }

    /** 서비스를 거쳐 후원한다. 재고와 프로젝트 누적 금액도 같이 반영된다. 만든 후원 ID를 돌려준다. */
    public static Long donate(AppUser user, Reward reward, BigDecimal amount, long quantity) {
        return bean(DonateService.class)
                .createDonate(user.getId(), new CreateDonateRequest(user.getId(), reward.getId(), amount, quantity))
                .getId();
    }

    /** 리포지토리로 후원 행만 바로 넣는다. 재고/누적 금액은 건드리지 않는다. */
    public static Donate donateRow(AppUser user, Reward reward, BigDecimal amount, long quantity) {
        return bean(DonateRepository.class).create(Donate.builder()
                .user(user)
                .reward(reward)
                .amount(amount)
                .quantity(quantity)
                .build());
    }

    public static Reward reloadReward(Long rewardId) {
        return bean(RewardRepository.class).findById(rewardId).orElseThrow();
    }

    public static Project reloadProject(Long projectId) {
        return bean(ProjectRepository.class).findById(projectId).orElseThrow();
    }
}
