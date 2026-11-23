package controller.external;

import exception.NotFoundException;
import fixture.ReviewFixtures;
import model.domain.AppUser;
import model.domain.Review;
import model.domain.ReviewLike;
import model.domain.Reward;
import model.dto.request.like.LikeRequest;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ReviewLikeRepository;
import repository.ReviewRepository;
import service.external.ReviewLikeService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReviewLikeRouter (/reivew/like)")
class ReviewLikeRouterTest extends ApiTest {

    /** 다른 사람이 쓴 리뷰 하나 */
    private Review someReview() {
        AppUser donor = createUser();
        Reward reward = ReviewFixtures.reviewableReward(createUser(), donor);
        return ReviewFixtures.review(donor, reward, "4.0");
    }

    private Review findReview(long id) {
        return bean(ReviewRepository.class).findById(id).orElseThrow(() -> new AssertionError("리뷰가 DB에 없다: " + id));
    }

    private List<ReviewLike> likesOf(Review review, AppUser user) {
        return bean(ReviewLikeRepository.class).findByReviewAndUser(review.getId(), user.getId());
    }

    private LikeRequest like(boolean likeType) {
        return LikeRequest.builder().likeType(likeType).build();
    }

    /**
     * id가 다른 유저의 리뷰 좋아요 id와 같은 리뷰를 만든다.
     * 리뷰 id와 리뷰 좋아요 id를 혼동하는 버그가 우연히 가려지지 않게 하려고 쓴다.
     */
    private Review reviewSharingIdWithOthersLike() {
        Review filler = someReview();
        Review review = someReview();
        for (int i = 0; i < 500; i++) {
            if (bean(ReviewLikeRepository.class).findById(review.getId()).isPresent()) {
                return review;
            }
            ReviewLike last = ReviewFixtures.reviewLike(createUser(), filler, true);
            if (last.getId() > review.getId()) {
                review = someReview();
            }
        }
        return fail("리뷰 id와 같은 id의 리뷰 좋아요를 만들지 못했다");
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/reivew/like/{review_id}")
    @DisplayName("POST /reivew/like/{review_id} - 리뷰 좋아요/싫어요")
    class CreateReviewLike {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("좋아요를 누르면 200과 좋아요 id를 받고 likeCount가 1 오른다")
            void like() {
                Review review = someReview();
                AppUser me = createUser();

                ApiResponse response = client.post("/reivew/like/" + review.getId())
                        .bearer(login(me))
                        .json(Map.of("likeType", true))
                        .send()
                        .assertStatus(200);

                List<ReviewLike> likes = likesOf(review, me);
                assertEquals(1, likes.size());
                assertEquals(likes.getFirst().getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/review/" + review.getId()).send().assertStatus(200);
                assertEquals(1L, after.json("/data/likeCount").asLong());
                assertEquals(0L, after.json("/data/dislikeCount").asLong());
            }

            @Test
            @DisplayName("싫어요를 누르면 dislikeCount가 1 오른다")
            void dislike() {
                Review review = someReview();

                client.post("/reivew/like/" + review.getId()).bearer(login(createUser()))
                        .json(Map.of("likeType", false)).send().assertStatus(200);

                Review saved = findReview(review.getId());
                assertEquals(0L, saved.getLikeCount());
                assertEquals(1L, saved.getDislikeCount());
            }

            @Test
            @DisplayName("같은 리뷰에 두 번 누르면 409이고 카운트는 1에 머문다")
            void duplicate() {
                // 서비스에 중복 검사는 없지만 DB unique 제약 위반을 SQLExecutor가 AlreadyExistException(409)으로 바꾼다
                Review review = someReview();
                String token = login(createUser());
                client.post("/reivew/like/" + review.getId()).bearer(token).json(Map.of("likeType", true)).send().assertStatus(200);

                client.post("/reivew/like/" + review.getId()).bearer(token).json(Map.of("likeType", true)).send().assertStatus(409);
                assertEquals(1L, findReview(review.getId()).getLikeCount());
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.post("/reivew/like/" + Long.MAX_VALUE).bearer(login(createUser()))
                        .json(Map.of("likeType", true)).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Review review = someReview();
                client.post("/reivew/like/" + review.getId()).json(Map.of("likeType", true)).send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                Review review = someReview();
                client.post("/reivew/like/" + review.getId()).bearer("bad.token.value")
                        .json(Map.of("likeType", true)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Review review = someReview();
                client.post("/reivew/like/" + review.getId()).bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.post("/reivew/like/abc").bearer(login(createUser()))
                        .json(Map.of("likeType", true)).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("좋아요 행이 생기고 리뷰의 likeCount가 오른다")
            void success() {
                Review review = someReview();
                AppUser me = createUser();

                IdResponse result = bean(ReviewLikeService.class).createReviewLike(review.getId(), me.getId(), like(true));

                ReviewLike saved = bean(ReviewLikeRepository.class).findById(result.getId()).orElseThrow();
                assertTrue(saved.getLikeType());
                assertEquals(review.getId(), saved.getReview().getId());
                assertEquals(me.getId(), saved.getUser().getId());
                assertEquals(1L, findReview(review.getId()).getLikeCount());
            }

            @Test
            @DisplayName("여러 사람이 누르면 그만큼 카운트가 쌓인다")
            void accumulates() {
                Review review = someReview();
                ReviewLikeService service = bean(ReviewLikeService.class);

                service.createReviewLike(review.getId(), createUser().getId(), like(true));
                service.createReviewLike(review.getId(), createUser().getId(), like(true));
                service.createReviewLike(review.getId(), createUser().getId(), like(false));

                Review saved = findReview(review.getId());
                assertEquals(2L, saved.getLikeCount());
                assertEquals(1L, saved.getDislikeCount());
            }

            @Test
            @DisplayName("없는 리뷰면 NotFoundException")
            void reviewNotFound() {
                AppUser me = createUser();
                assertThrows(NotFoundException.class, () ->
                        bean(ReviewLikeService.class).createReviewLike(Long.MAX_VALUE, me.getId(), like(true)));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException이고 카운트는 그대로다")
            void userNotFound() {
                Review review = someReview();

                assertThrows(NotFoundException.class, () ->
                        bean(ReviewLikeService.class).createReviewLike(review.getId(), Long.MAX_VALUE, like(true)));
                assertEquals(0L, findReview(review.getId()).getLikeCount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/reivew/like/{review_id}")
    @DisplayName("PATCH /reivew/like/{review_id} - 좋아요/싫어요 변경")
    class UpdateReviewLike {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("[BUG] 좋아요를 싫어요로 바꾸면 200과 좋아요 id를 받고 카운트와 좋아요 행이 바뀐다")
            void switchesToDislike() {
                // 버그: ReviewLikeService.editReviewLike가 reviewLike id 대신 review id로 REVIEW_LIKE 행을 update한다
                Review review = reviewSharingIdWithOthersLike();
                AppUser me = createUser();
                ReviewLike mine = ReviewFixtures.reviewLike(me, review, true);

                ApiResponse response = client.patch("/reivew/like/" + review.getId())
                        .bearer(login(me))
                        .json(Map.of("likeType", false))
                        .send()
                        .assertStatus(200);

                assertEquals(mine.getId().longValue(), response.json("/id").asLong(), response::toString);
                assertFalse(likesOf(review, me).getFirst().getLikeType());
                Review saved = findReview(review.getId());
                assertEquals(0L, saved.getLikeCount());
                assertEquals(1L, saved.getDislikeCount());
            }

            @Test
            @DisplayName("[BUG] 누른 적이 없으면 404")
            void noLike() {
                // 버그: editReviewLike가 findByReviewAndUser(...).getFirst()를 바로 불러 빈 목록이면 NoSuchElementException(500)
                Review review = someReview();
                client.patch("/reivew/like/" + review.getId()).bearer(login(createUser()))
                        .json(Map.of("likeType", false)).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Review review = someReview();
                client.patch("/reivew/like/" + review.getId()).json(Map.of("likeType", false)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Review review = someReview();
                client.patch("/reivew/like/" + review.getId()).bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.patch("/reivew/like/abc").bearer(login(createUser()))
                        .json(Map.of("likeType", false)).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("[BUG] 싫어요를 좋아요로 바꾸면 카운트가 옮겨지고 좋아요 행의 likeType이 바뀐다")
            void switchesToLike() {
                // 버그: editReviewLike가 reviewLikeRepository.update에 review.getId()를 넘겨 엉뚱한 행을 바꾸거나 NotFound를 던진다
                Review review = reviewSharingIdWithOthersLike();
                AppUser me = createUser();
                ReviewLike mine = ReviewFixtures.reviewLike(me, review, false);

                IdResponse result = bean(ReviewLikeService.class).editReviewLike(review.getId(), me.getId(), like(true));

                assertEquals(mine.getId(), result.getId());
                assertTrue(bean(ReviewLikeRepository.class).findById(mine.getId()).orElseThrow().getLikeType());
                Review saved = findReview(review.getId());
                assertEquals(1L, saved.getLikeCount());
                assertEquals(0L, saved.getDislikeCount());
            }

            @Test
            @DisplayName("[BUG] 다른 사람의 좋아요 행은 건드리지 않는다")
            void doesNotTouchOthers() {
                // 버그: editReviewLike가 review id와 같은 id의 REVIEW_LIKE 행을 update한다
                Review review = reviewSharingIdWithOthersLike();
                AppUser me = createUser();
                ReviewFixtures.reviewLike(me, review, true);
                List<ReviewLike> before = bean(ReviewLikeRepository.class).findAll();

                try {
                    bean(ReviewLikeService.class).editReviewLike(review.getId(), me.getId(), like(false));
                } catch (RuntimeException ignored) {
                    // 예외 여부와 상관없이 다른 행이 바뀌지 않았는지만 본다
                }

                for (ReviewLike other : before) {
                    if (other.getUser().getId().equals(me.getId())) {
                        continue;
                    }
                    ReviewLike now = bean(ReviewLikeRepository.class).findById(other.getId()).orElseThrow();
                    assertEquals(other.getLikeType(), now.getLikeType(), "다른 유저의 좋아요 행이 바뀌었다: " + other.getId());
                }
            }

            @Test
            @DisplayName("[BUG] 누른 적이 없으면 NotFoundException")
            void noLike() {
                // 버그: getFirst()가 빈 목록에서 NoSuchElementException을 던진다
                Review review = someReview();
                AppUser me = createUser();
                assertThrows(NotFoundException.class, () ->
                        bean(ReviewLikeService.class).editReviewLike(review.getId(), me.getId(), like(true)));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/reivew/like/{review_id}")
    @DisplayName("DELETE /reivew/like/{review_id} - 좋아요/싫어요 취소")
    class DeleteReviewLike {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("취소하면 200과 result=true를 받고 행이 사라지며 카운트가 내려간다")
            void deletesLike() {
                Review review = someReview();
                AppUser me = createUser();
                ReviewFixtures.reviewLike(me, review, true);

                ApiResponse response = client.delete("/reivew/like/" + review.getId())
                        .bearer(login(me)).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                assertTrue(likesOf(review, me).isEmpty());
                assertEquals(0L, findReview(review.getId()).getLikeCount());
            }

            @Test
            @DisplayName("[BUG] 누른 적이 없으면 404")
            void noLike() {
                // 버그: deleteReviewLike가 findByReviewAndUser(...).getFirst()를 바로 불러 빈 목록이면 NoSuchElementException(500)
                Review review = someReview();
                client.delete("/reivew/like/" + review.getId()).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Review review = someReview();
                client.delete("/reivew/like/" + review.getId()).send().assertStatus(401);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.delete("/reivew/like/abc").bearer(login(createUser())).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("싫어요를 취소하면 dislikeCount만 내려가고 다른 사람 좋아요는 남는다")
            void dislikeCanceled() {
                Review review = someReview();
                AppUser me = createUser();
                AppUser other = createUser();
                ReviewFixtures.reviewLike(other, review, true);
                ReviewFixtures.reviewLike(me, review, false);

                assertTrue(bean(ReviewLikeService.class).deleteReviewLike(review.getId(), me.getId()).getResult());

                Review saved = findReview(review.getId());
                assertEquals(1L, saved.getLikeCount());
                assertEquals(0L, saved.getDislikeCount());
                assertTrue(likesOf(review, me).isEmpty());
                assertEquals(1, likesOf(review, other).size());
            }

            @Test
            @DisplayName("[BUG] 누른 적이 없으면 NotFoundException")
            void noLike() {
                // 버그: getFirst()가 빈 목록에서 NoSuchElementException을 던진다
                Review review = someReview();
                AppUser me = createUser();
                assertThrows(NotFoundException.class, () ->
                        bean(ReviewLikeService.class).deleteReviewLike(review.getId(), me.getId()));
            }
        }
    }
}
