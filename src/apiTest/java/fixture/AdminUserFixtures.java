package fixture;

import constant.Role;
import constant.SocialProvider;
import model.domain.*;
import repository.*;
import support.TestServer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 관리자 라우터(/internal/review, /internal/account, /internal/user) 테스트에서 쓰는 데이터 생성 도우미.
 * 서버와 같은 리포지토리 인스턴스를 꺼내 DB에 직접 넣는다.
 */
public final class AdminUserFixtures {
    /** 어떤 테이블에도 없을 id */
    public static final long MISSING_ID = Long.MAX_VALUE;

    private AdminUserFixtures() {
    }

    private static <T> T bean(Class<T> type) {
        return TestServer.get().bean(type);
    }

    public static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 모든 값을 채운 유저 */
    public static AppUser fullUser() {
        String key = unique("u");
        return bean(UserRepository.class).create(AppUser.builder()
                .name("테스트 " + key)
                .nickname(key)
                .email(key + "@test.com")
                .postal("12345")
                .address("서울시 " + key)
                .phone("010-" + key)
                .build());
    }

    /**
     * 권한 마스터 데이터(USER_ROLE). 테이블을 새로 만들면 비어 있을 수 있으므로 없으면 만든다.
     */
    public static UserRole role(Role role) {
        UserRoleRepository repository = bean(UserRoleRepository.class);
        return repository.findByRole(role)
                .orElseGet(() -> repository.create(UserRole.builder().role(role).build()));
    }

    /** 유저에게 권한을 직접 연결한다 */
    public static UserRoleRelation grant(AppUser user, Role role) {
        return bean(UserRoleRelationRepository.class).create(UserRoleRelation.builder()
                .user(user)
                .role(role(role))
                .build());
    }

    public static Project project(AppUser maker) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .title(unique("project"))
                .text("프로젝트 설명")
                .endDate(LocalDateTime.now().plusDays(30))
                .purposeAmount(new BigDecimal("10000"))
                .build());
    }

    public static Reward reward(Project project) {
        return bean(RewardRepository.class).create(Reward.builder()
                .project(project)
                .name(unique("reward"))
                .text("리워드 설명")
                .rewardPerAmount(new BigDecimal("1000"))
                .maxQuantity(10L)
                .remainQuantity(10L)
                .provideDate(LocalDateTime.now().plusDays(40))
                .build());
    }

    /** 새 메이커와 프로젝트를 만들고 그 밑에 리워드를 하나 만든다 */
    public static Reward reward(AppUser maker) {
        return reward(project(maker));
    }

    public static Review review(AppUser user, Reward reward, String text, String rating) {
        return bean(ReviewRepository.class).create(Review.builder()
                .user(user)
                .reward(reward)
                .text(text)
                .rating(new BigDecimal(rating))
                .build());
    }

    public static SocialAccount socialAccount(AppUser user, SocialProvider provider) {
        return bean(SocialAccountRepository.class).createSocialAccount(unique("social"), user, provider);
    }

    public static LocalAccount localAccount(AppUser user) {
        return bean(LocalAccountRepository.class).create(LocalAccount.builder()
                .user(user)
                .loginId(unique("login"))
                .password("hashed-password")
                .salt("salt")
                .build());
    }
}
