package fixture;

import constant.ProjectState;
import model.domain.*;
import repository.*;
import support.TestServer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 프로젝트/프로젝트 좋아요 테스트용 데이터.
 * 테스트마다 겹치지 않는 값으로 카테고리, 프로젝트, 리워드, 후원, 거절 사유를 만든다.
 */
public final class ProjectFixtures {
    private ProjectFixtures() {
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

    /** 상태만 지정한 프로젝트 (카테고리 없음) */
    public static Project project(AppUser maker, ProjectState state) {
        return project(maker, null, state);
    }

    /** 카테고리와 상태를 지정한 프로젝트 */
    public static Project project(AppUser maker, Category category, ProjectState state) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .category(category)
                .title(unique("project"))
                .text("프로젝트 테스트 본문")
                .state(state)
                .purposeAmount(new BigDecimal("10000"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());
    }

    /** 원하는 필드를 직접 채운 프로젝트. builder에 maker 등 필수 값은 넣어서 넘긴다. */
    public static Project project(Project.ProjectBuilder<?, ?> builder) {
        return bean(ProjectRepository.class).create(builder.build());
    }

    /** 좋아요/싫어요 수를 지정한 프로젝트 */
    public static Project projectWithLikes(AppUser maker, ProjectState state, long likeCount, long dislikeCount) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .title(unique("project"))
                .text("좋아요 테스트 프로젝트")
                .state(state)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .purposeAmount(new BigDecimal("10000"))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());
    }

    /** 프로젝트에 딸린 리워드 */
    public static Reward reward(Project project) {
        return bean(RewardRepository.class).create(Reward.builder()
                .project(project)
                .name(unique("reward"))
                .text("프로젝트 테스트 리워드")
                .rewardPerAmount(new BigDecimal("1000"))
                .maxQuantity(10L)
                .remainQuantity(10L)
                .provideDate(LocalDateTime.now().plusDays(20))
                .build());
    }

    /** 유저가 리워드에 후원한 내역 */
    public static Donate donate(AppUser user, Reward reward) {
        return bean(DonateRepository.class).create(Donate.builder()
                .user(user)
                .reward(reward)
                .amount(new BigDecimal("1000"))
                .quantity(1L)
                .build());
    }

    /** 프로젝트 거절 사유 */
    public static DenyProject deny(Project project, String reason) {
        return bean(DenyProjectRepository.class).create(DenyProject.builder()
                .project(project)
                .reason(reason)
                .build());
    }

    /** 프로젝트 좋아요(likeType=true) 또는 싫어요(false). 프로젝트의 카운트는 바꾸지 않는다 */
    public static ProjectLike like(Project project, AppUser user, boolean likeType) {
        return bean(ProjectLikeRepository.class).create(ProjectLike.builder()
                .project(project)
                .user(user)
                .likeType(likeType)
                .build());
    }
}
