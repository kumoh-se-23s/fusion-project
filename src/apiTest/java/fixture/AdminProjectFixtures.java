package fixture;

import constant.ProjectState;
import model.domain.*;
import repository.*;
import support.TestServer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 관리자 카테고리/프로젝트/거절 사유 테스트용 데이터.
 * 다른 테스트의 행이 섞여 있으므로 이름과 제목은 항상 겹치지 않게 만든다.
 */
public final class AdminProjectFixtures {
    private AdminProjectFixtures() {
    }

    private static <T> T bean(Class<T> type) {
        return TestServer.get().bean(type);
    }

    public static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 이름이 겹치지 않는 카테고리 */
    public static Category category() {
        String name = unique("category");
        return bean(CategoryRepository.class).create(Category.builder()
                .name(name)
                .description(name + " 설명")
                .build());
    }

    /** 닉네임이 겹치지 않는 메이커 */
    public static AppUser maker() {
        String key = unique("m");
        return bean(UserRepository.class).create(AppUser.builder()
                .name("메이커 " + key)
                .nickname(key)
                .email(key + "@test.com")
                .build());
    }

    /** 상태를 지정한 프로젝트 */
    public static Project project(AppUser maker, Category category, ProjectState state) {
        return project(maker, category, state, new BigDecimal("10000"), BigDecimal.ZERO);
    }

    /** 목표 금액과 모인 금액까지 지정한 프로젝트 */
    public static Project project(AppUser maker, Category category, ProjectState state,
                                  BigDecimal purposeAmount, BigDecimal donateAmount) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .category(category)
                .title(unique("project"))
                .text("관리자 테스트 프로젝트")
                .state(state)
                .purposeAmount(purposeAmount)
                .donateAmount(donateAmount)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build());
    }

    /** 새 메이커, 새 카테고리로 만든 프로젝트 */
    public static Project project(ProjectState state) {
        return project(maker(), category(), state);
    }

    /** 프로젝트에 딸린 리워드 */
    public static Reward reward(Project project) {
        return bean(RewardRepository.class).create(Reward.builder()
                .project(project)
                .name(unique("reward"))
                .text("관리자 테스트 리워드")
                .rewardPerAmount(new BigDecimal("1000"))
                .maxQuantity(100L)
                .remainQuantity(100L)
                .provideDate(LocalDateTime.now().plusDays(40))
                .build());
    }

    /** 리워드에 대한 후원 한 건 (서포터 한 명) */
    public static Donate donate(AppUser user, Reward reward) {
        return bean(DonateRepository.class).create(Donate.builder()
                .user(user)
                .reward(reward)
                .amount(new BigDecimal("1000"))
                .quantity(1L)
                .build());
    }

    /** 좋아요(true)/싫어요(false) 한 건. 프로젝트의 카운트는 따로 맞춰야 한다. */
    public static ProjectLike like(Project project, AppUser user, boolean likeType) {
        return bean(ProjectLikeRepository.class).create(ProjectLike.builder()
                .project(project)
                .user(user)
                .likeType(likeType)
                .build());
    }

    /** 거절 사유 한 건 */
    public static DenyProject denyProject(Project project, String reason) {
        return bean(DenyProjectRepository.class).create(DenyProject.builder()
                .project(project)
                .reason(reason)
                .build());
    }

    public static Project reload(Project project) {
        return bean(ProjectRepository.class).findById(project.getId()).orElseThrow();
    }
}
