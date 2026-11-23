package fixture;

import constant.ProjectState;
import model.domain.*;
import repository.*;
import support.TestServer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 리뷰/리뷰 좋아요 테스트용 데이터.
 * 리뷰는 (완료된 프로젝트 -> 리워드 -> 배송 완료된 후원) 이 있어야 쓸 수 있으므로 그 줄기를 한 번에 만든다.
 */
public final class ReviewFixtures {
    private ReviewFixtures() {
    }

    private static <T> T bean(Class<T> type) {
        return TestServer.get().bean(type);
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 상태를 지정한 프로젝트 */
    public static Project project(AppUser maker, ProjectState state) {
        return bean(ProjectRepository.class).create(Project.builder()
                .maker(maker)
                .title(unique("project"))
                .text("리뷰 테스트 프로젝트")
                .state(state)
                .purposeAmount(new BigDecimal("10000"))
                .endDate(LocalDateTime.now().minusDays(1))
                .build());
    }

    /** 프로젝트에 딸린 리워드 */
    public static Reward reward(Project project) {
        return bean(RewardRepository.class).create(Reward.builder()
                .project(project)
                .name(unique("reward"))
                .text("리뷰 테스트 리워드")
                .rewardPerAmount(new BigDecimal("1000"))
                .maxQuantity(10L)
                .remainQuantity(10L)
                .provideDate(LocalDateTime.now().minusDays(1))
                .build());
    }

    /** 후원. delivered가 true면 배송 완료 상태 */
    public static Donate donate(AppUser user, Reward reward, boolean delivered) {
        return bean(DonateRepository.class).create(Donate.builder()
                .user(user)
                .reward(reward)
                .amount(new BigDecimal("1000"))
                .quantity(1L)
                .state(delivered)
                .build());
    }

    /** donor가 리뷰를 쓸 수 있는 리워드 (완료된 프로젝트 + 배송 완료된 후원) */
    public static Reward reviewableReward(AppUser maker, AppUser donor) {
        Reward reward = reward(project(maker, ProjectState.COMPLETED));
        donate(donor, reward, true);
        return reward;
    }

    /** 서비스를 거치지 않고 리뷰를 바로 DB에 만든다 */
    public static Review review(AppUser user, Reward reward, String rating) {
        return bean(ReviewRepository.class).create(Review.builder()
                .user(user)
                .reward(reward)
                .text(unique("review"))
                .rating(new BigDecimal(rating))
                .build());
    }

    /** 서비스를 거치지 않고 리뷰 좋아요를 바로 DB에 만든다 (리뷰의 카운트도 같이 맞춘다) */
    public static ReviewLike reviewLike(AppUser user, Review review, boolean likeType) {
        ReviewRepository reviewRepository = bean(ReviewRepository.class);
        Review current = reviewRepository.findById(review.getId()).orElseThrow();
        if (likeType) {
            reviewRepository.updateByLike(review.getId(), current.getLikeCount() + 1);
        } else {
            reviewRepository.updateByDislike(review.getId(), current.getDislikeCount() + 1);
        }
        return bean(ReviewLikeRepository.class).create(ReviewLike.builder()
                .user(user)
                .review(review)
                .likeType(likeType)
                .build());
    }
}
