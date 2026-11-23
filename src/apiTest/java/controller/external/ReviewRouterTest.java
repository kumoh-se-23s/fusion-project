package controller.external;

import com.fasterxml.jackson.databind.JsonNode;
import constant.ProjectState;
import exception.*;
import fixture.ReviewFixtures;
import model.domain.AppUser;
import model.domain.Project;
import model.domain.Review;
import model.domain.Reward;
import model.dto.ReviewData;
import model.dto.request.review.CreateReviewRequest;
import model.dto.request.review.ReviewDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ReviewRepository;
import service.external.ReviewCommandService;
import service.external.ReviewQueryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReviewRouter (/review)")
class ReviewRouterTest extends ApiTest {

    /** 리뷰를 쓸 수 있는 상태의 후원자와 리워드 */
    private record Reviewable(AppUser donor, Reward reward) {
    }

    private Reviewable reviewable() {
        AppUser maker = createUser();
        AppUser donor = createUser();
        return new Reviewable(donor, ReviewFixtures.reviewableReward(maker, donor));
    }

    /** 다른 사람이 쓴 리뷰 하나 */
    private Review someReview() {
        Reviewable r = reviewable();
        return ReviewFixtures.review(r.donor(), r.reward(), "4.0");
    }

    private Map<String, Object> createBody(long rewardId, String text, Object rating) {
        return Map.of("rewardId", rewardId, "request", Map.of("text", text, "rating", rating));
    }

    private CreateReviewRequest createRequest(long rewardId, String text, String rating) {
        return CreateReviewRequest.builder()
                .rewardId(rewardId)
                .request(ReviewDataRequest.builder().text(text).rating(new BigDecimal(rating)).build())
                .build();
    }

    private Review findReview(long id) {
        return bean(ReviewRepository.class).findById(id).orElseThrow(() -> new AssertionError("리뷰가 DB에 없다: " + id));
    }

    private Set<Long> ids(JsonNode datas) {
        return StreamSupport.stream(datas.spliterator(), false)
                .map(node -> node.path("id").asLong())
                .collect(Collectors.toSet());
    }

    private Set<Long> ids(DataCollectionResponse<ReviewData> result) {
        return result.getDatas().stream().map(ReviewData::getId).collect(Collectors.toSet());
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/review")
    @DisplayName("POST /review - 리뷰 작성")
    class CreateReview {

        @Nested
        @DisplayName("Http")
        class Http {
            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 배송 완료된 후원자는 200과 새 리뷰 id를 받고 DB에 리뷰가 생긴다")
            void createsReview() {
                Reviewable r = reviewable();
                String text = unique("좋아요");

                ApiResponse response = client.post("/review")
                        .bearer(login(r.donor()))
                        .json(createBody(r.reward().getId(), text, 4.5))
                        .send()
                        .assertStatus(200);

                long id = response.json("/id").asLong();
                assertTrue(id > 0, response::toString);
                Review saved = findReview(id);
                assertEquals(text, saved.getText());
                assertEquals(0, new BigDecimal("4.5").compareTo(saved.getRating()));
                assertEquals(r.donor().getId(), saved.getUser().getId());
                assertEquals(r.reward().getId(), saved.getReward().getId());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Reviewable r = reviewable();
                client.post("/review").json(createBody(r.reward().getId(), "text", 4.0)).send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                Reviewable r = reviewable();
                client.post("/review").bearer("not.a.jwt")
                        .json(createBody(r.reward().getId(), "text", 4.0)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/review").bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("깨진 JSON이면 422")
            void brokenJson() {
                client.post("/review").bearer(login(createUser())).rawJson("{\"rewardId\":").send().assertStatus(422);
            }

            @Test
            @DisplayName("없는 리워드면 404")
            void rewardNotFound() {
                client.post("/review").bearer(login(createUser()))
                        .json(createBody(Long.MAX_VALUE, "text", 4.0)).send().assertStatus(404);
            }

            @Test
            @DisplayName("후원하지 않은 리워드에 쓰면 409")
            void notDonated() {
                Reviewable r = reviewable();
                client.post("/review").bearer(login(createUser()))
                        .json(createBody(r.reward().getId(), "text", 4.0)).send().assertStatus(409);
            }

            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 같은 리워드에 두 번 쓰면 409")
            void duplicate() {
                Reviewable r = reviewable();
                String token = login(r.donor());
                client.post("/review").bearer(token).json(createBody(r.reward().getId(), "first", 4.0)).send().assertStatus(200);
                client.post("/review").bearer(token).json(createBody(r.reward().getId(), "second", 3.0)).send().assertStatus(409);
            }

            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 평점이 5.0을 넘으면 400")
            void ratingOutOfRange() {
                Reviewable r = reviewable();
                client.post("/review").bearer(login(r.donor()))
                        .json(createBody(r.reward().getId(), "text", 5.5)).send().assertStatus(400);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 완료된 프로젝트의 배송 완료 후원자는 리뷰를 쓸 수 있고 좋아요 수는 0으로 시작한다")
            void success() {
                Reviewable r = reviewable();

                IdResponse result = bean(ReviewCommandService.class)
                        .createReview(createRequest(r.reward().getId(), "최고", "5.0"), r.donor().getId());

                Review saved = findReview(result.getId());
                assertEquals("최고", saved.getText());
                assertEquals(0, new BigDecimal("5.0").compareTo(saved.getRating()));
                assertEquals(0L, saved.getLikeCount());
                assertEquals(0L, saved.getDislikeCount());
            }

            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 평점 경계값 0.0도 허용한다")
            void minRating() {
                Reviewable r = reviewable();

                IdResponse result = bean(ReviewCommandService.class)
                        .createReview(createRequest(r.reward().getId(), "별로", "0.0"), r.donor().getId());

                assertEquals(0, BigDecimal.ZERO.compareTo(findReview(result.getId()).getRating()));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException")
            void userNotFound() {
                Reviewable r = reviewable();
                assertThrows(NotFoundException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(r.reward().getId(), "t", "3.0"), Long.MAX_VALUE));
            }

            @Test
            @DisplayName("없는 리워드면 NotFoundException")
            void rewardNotFound() {
                AppUser user = createUser();
                assertThrows(NotFoundException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(Long.MAX_VALUE, "t", "3.0"), user.getId()));
            }

            @Test
            @DisplayName("후원하지 않았으면 NotCompleteProjectException")
            void notDonated() {
                Reviewable r = reviewable();
                AppUser stranger = createUser();
                assertThrows(NotCompleteProjectException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(r.reward().getId(), "t", "3.0"), stranger.getId()));
            }

            @Test
            @DisplayName("후원했지만 아직 배송 전이면 NotCompleteProjectException")
            void notDelivered() {
                AppUser donor = createUser();
                Reward reward = ReviewFixtures.reward(ReviewFixtures.project(createUser(), ProjectState.COMPLETED));
                ReviewFixtures.donate(donor, reward, false);

                assertThrows(NotCompleteProjectException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(reward.getId(), "t", "3.0"), donor.getId()));
            }

            @Test
            @DisplayName("배송은 끝났어도 프로젝트가 완료 상태가 아니면 NotCompleteProjectException")
            void projectNotCompleted() {
                AppUser donor = createUser();
                Reward reward = ReviewFixtures.reward(ReviewFixtures.project(createUser(), ProjectState.IN_PROGRESS));
                ReviewFixtures.donate(donor, reward, true);

                assertThrows(NotCompleteProjectException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(reward.getId(), "t", "3.0"), donor.getId()));
            }

            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 이미 쓴 리뷰가 있으면 AlreadyExistException이고 리뷰는 하나만 남는다")
            void duplicate() {
                Reviewable r = reviewable();
                ReviewCommandService service = bean(ReviewCommandService.class);
                service.createReview(createRequest(r.reward().getId(), "첫 리뷰", "3.0"), r.donor().getId());

                assertThrows(AlreadyExistException.class, () ->
                        service.createReview(createRequest(r.reward().getId(), "두 번째", "4.0"), r.donor().getId()));
                assertEquals(1, bean(ReviewRepository.class).findByReward(r.reward().getId()).size());
            }

            // 버그: ReviewDataRequest에 @BindDomain이 없어 createReview의 Serializer.toDomain(dto.getRequest(), Review.class)가 항상 SerializeException(500)
            @Test
            @DisplayName("[BUG] 평점이 범위를 벗어나면 OutOfRangeException이고 리뷰가 생기지 않는다")
            void ratingOutOfRange() {
                Reviewable r = reviewable();

                assertThrows(OutOfRangeException.class, () -> bean(ReviewCommandService.class)
                        .createReview(createRequest(r.reward().getId(), "t", "5.1"), r.donor().getId()));
                assertTrue(bean(ReviewRepository.class).findByReward(r.reward().getId()).isEmpty());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/review/{review_id}")
    @DisplayName("GET /review/{review_id} - 리뷰 단건 조회")
    class GetReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("로그인 없이 200과 리뷰 데이터를 돌려준다")
            void returnsReview() {
                Review review = someReview();

                ApiResponse response = client.get("/review/" + review.getId()).send().assertStatus(200);

                JsonNode data = response.json("/data");
                assertEquals(review.getId().longValue(), data.path("id").asLong());
                assertEquals(review.getReward().getId().longValue(), data.path("rewardId").asLong());
                assertEquals(review.getUser().getId().longValue(), data.path("userId").asLong());
                assertEquals(review.getText(), data.path("text").asText());
                assertEquals(0, new BigDecimal("4.0").compareTo(data.path("rating").decimalValue()), response::toString);
                assertEquals(0L, data.path("likeCount").asLong());
                assertEquals(0L, data.path("dislikeCount").asLong());
                assertFalse(data.path("createdAt").isMissingNode(), response::toString);
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.get("/review/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.get("/review/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("DB의 리뷰가 DTO로 옮겨진다")
            void mapsFields() {
                Review review = someReview();

                ReviewData data = bean(ReviewQueryService.class).getReview(review.getId()).getData();

                assertEquals(review.getId(), data.getId());
                assertEquals(review.getReward().getId(), data.getRewardId());
                assertEquals(review.getUser().getId(), data.getUserId());
                assertEquals(review.getText(), data.getText());
                assertEquals(0, review.getRating().compareTo(data.getRating()));
            }

            @Test
            @DisplayName("없는 리뷰면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(ReviewQueryService.class).getReview(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/review/my")
    @DisplayName("GET /review/my - 내 리뷰 목록")
    class GetMyReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 내가 쓴 리뷰만 돌려준다")
            void returnsOnlyMine() {
                AppUser me = createUser();
                Review mine1 = ReviewFixtures.review(me, ReviewFixtures.reviewableReward(createUser(), me), "3.0");
                Review mine2 = ReviewFixtures.review(me, ReviewFixtures.reviewableReward(createUser(), me), "4.0");
                Review others = someReview();

                ApiResponse response = client.get("/review/my").bearer(login(me)).send().assertStatus(200);

                assertEquals(Set.of(mine1.getId(), mine2.getId()), ids(response.json("/datas")), response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                assertFalse(ids(response.json("/datas")).contains(others.getId()));
            }

            @Test
            @DisplayName("리뷰가 없으면 빈 목록")
            void empty() {
                ApiResponse response = client.get("/review/my").bearer(login(createUser())).send().assertStatus(200);
                assertEquals(0, response.json("/datas").size());
                assertEquals(0L, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.get("/review/my").send().assertStatus(401);
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.get("/review/my").bearer("broken-token").send().assertStatus(401);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("유저의 리뷰 수와 amount가 같다")
            void amountMatches() {
                AppUser me = createUser();
                ReviewFixtures.review(me, ReviewFixtures.reviewableReward(createUser(), me), "3.0");

                DataCollectionResponse<ReviewData> result = bean(ReviewQueryService.class).getUserReview(me.getId());

                assertEquals(1L, result.getAmount());
                assertEquals(me.getId(), result.getDatas().iterator().next().getUserId());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/review/reward/{reward_id}")
    @DisplayName("GET /review/reward/{reward_id} - 리워드별 리뷰 목록")
    class GetRewardReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 해당 리워드의 리뷰만 돌려준다")
            void returnsRewardReviews() {
                AppUser maker = createUser();
                AppUser donor1 = createUser();
                AppUser donor2 = createUser();
                Reward reward = ReviewFixtures.reviewableReward(maker, donor1);
                ReviewFixtures.donate(donor2, reward, true);
                Review r1 = ReviewFixtures.review(donor1, reward, "3.0");
                Review r2 = ReviewFixtures.review(donor2, reward, "5.0");
                Review other = someReview();

                ApiResponse response = client.get("/review/reward/" + reward.getId()).send().assertStatus(200);

                assertEquals(Set.of(r1.getId(), r2.getId()), ids(response.json("/datas")), response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                assertFalse(ids(response.json("/datas")).contains(other.getId()));
            }

            @Test
            @DisplayName("없는 리워드면 404")
            void notFound() {
                client.get("/review/reward/" + Long.MAX_VALUE).send().assertStatus(404);
            }

            @Test
            @DisplayName("reward_id가 숫자가 아니면 422")
            void badPathParam() {
                client.get("/review/reward/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("리뷰가 없는 리워드는 빈 목록")
            void empty() {
                Reward reward = ReviewFixtures.reward(ReviewFixtures.project(createUser(), ProjectState.COMPLETED));

                DataCollectionResponse<ReviewData> result = bean(ReviewQueryService.class).getRewardReview(reward.getId());

                assertEquals(0L, result.getAmount());
                assertTrue(result.getDatas().isEmpty());
            }

            @Test
            @DisplayName("없는 리워드면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(ReviewQueryService.class).getRewardReview(Long.MAX_VALUE));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/review/project/{project_id}")
    @DisplayName("GET /review/project/{project_id} - 프로젝트별 리뷰 목록")
    class GetProjectReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 프로젝트의 모든 리워드에 달린 리뷰를 돌려준다")
            void returnsProjectReviews() {
                AppUser donor = createUser();
                Project project = ReviewFixtures.project(createUser(), ProjectState.COMPLETED);
                Reward rewardA = ReviewFixtures.reward(project);
                Reward rewardB = ReviewFixtures.reward(project);
                Review a = ReviewFixtures.review(donor, rewardA, "3.0");
                Review b = ReviewFixtures.review(donor, rewardB, "4.0");
                Review other = someReview();

                ApiResponse response = client.get("/review/project/" + project.getId()).send().assertStatus(200);

                assertEquals(Set.of(a.getId(), b.getId()), ids(response.json("/datas")), response::toString);
                assertEquals(2L, response.json("/amount").asLong());
                assertFalse(ids(response.json("/datas")).contains(other.getId()));
            }

            @Test
            @DisplayName("리워드가 없는 프로젝트는 200과 빈 목록")
            void projectWithoutRewards() {
                Project project = ReviewFixtures.project(createUser(), ProjectState.COMPLETED);

                ApiResponse response = client.get("/review/project/" + project.getId()).send().assertStatus(200);

                assertEquals(0, response.json("/datas").size());
                assertEquals(0L, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void badPathParam() {
                client.get("/review/project/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("리워드는 있지만 리뷰가 없으면 빈 목록")
            void noReviews() {
                Project project = ReviewFixtures.project(createUser(), ProjectState.COMPLETED);
                ReviewFixtures.reward(project);

                DataCollectionResponse<ReviewData> result = bean(ReviewQueryService.class).getProjectReview(project.getId());

                assertEquals(0L, result.getAmount());
            }

            @Test
            @DisplayName("다른 프로젝트의 리뷰는 섞이지 않는다")
            void onlyThisProject() {
                AppUser donor = createUser();
                Project project = ReviewFixtures.project(createUser(), ProjectState.COMPLETED);
                Review mine = ReviewFixtures.review(donor, ReviewFixtures.reward(project), "2.5");
                someReview();

                DataCollectionResponse<ReviewData> result = bean(ReviewQueryService.class).getProjectReview(project.getId());

                assertEquals(Set.of(mine.getId()), ids(result));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/review/{review_id}")
    @DisplayName("PATCH /review/{review_id} - 리뷰 수정")
    class UpdateReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("작성자는 200과 리뷰 id를 받고 변경 내용이 조회에 반영된다")
            void updatesReview() {
                Review review = someReview();
                String text = unique("수정됨");

                ApiResponse response = client.patch("/review/" + review.getId())
                        .bearer(login(review.getUser().getId()))
                        .json(Map.of("text", text, "rating", 1.5))
                        .send()
                        .assertStatus(200);

                assertEquals(review.getId().longValue(), response.json("/id").asLong());
                ApiResponse after = client.get("/review/" + review.getId()).send().assertStatus(200);
                assertEquals(text, after.json("/data/text").asText());
                assertEquals(0, new BigDecimal("1.5").compareTo(after.json("/data/rating").decimalValue()));
            }

            @Test
            @DisplayName("[BUG] 텍스트만 보내는 부분 수정도 200이고 평점은 그대로다")
            void partialUpdate() {
                // 버그: ReviewCommandService.editReview가 rating null 체크 없이 compareTo를 불러 NPE(500)가 난다
                Review review = someReview();
                String text = unique("텍스트만");

                client.patch("/review/" + review.getId())
                        .bearer(login(review.getUser().getId()))
                        .json(Map.of("text", text))
                        .send()
                        .assertStatus(200);

                Review saved = findReview(review.getId());
                assertEquals(text, saved.getText());
                assertEquals(0, new BigDecimal("4.0").compareTo(saved.getRating()));
            }

            @Test
            @DisplayName("작성자가 아니면 403")
            void notOwner() {
                Review review = someReview();
                client.patch("/review/" + review.getId()).bearer(login(createUser()))
                        .json(Map.of("text", "x", "rating", 3.0)).send().assertStatus(403);
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.patch("/review/" + Long.MAX_VALUE).bearer(login(createUser()))
                        .json(Map.of("text", "x", "rating", 3.0)).send().assertStatus(404);
            }

            @Test
            @DisplayName("평점이 범위를 벗어나면 400")
            void ratingOutOfRange() {
                Review review = someReview();
                client.patch("/review/" + review.getId()).bearer(login(review.getUser().getId()))
                        .json(Map.of("rating", -1.0)).send().assertStatus(400);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Review review = someReview();
                client.patch("/review/" + review.getId()).json(Map.of("text", "x", "rating", 3.0)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Review review = someReview();
                client.patch("/review/" + review.getId()).bearer(login(review.getUser().getId())).send().assertStatus(422);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.patch("/review/abc").bearer(login(createUser()))
                        .json(Map.of("text", "x", "rating", 3.0)).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            private ReviewDataRequest request(String text, String rating) {
                return ReviewDataRequest.builder().text(text).rating(rating == null ? null : new BigDecimal(rating)).build();
            }

            @Test
            @DisplayName("작성자가 수정하면 DB에 반영된다")
            void success() {
                Review review = someReview();

                IdResponse result = bean(ReviewCommandService.class)
                        .editReview(review.getUser().getId(), review.getId(), request("새 내용", "2.0"));

                assertEquals(review.getId(), result.getId());
                Review saved = findReview(review.getId());
                assertEquals("새 내용", saved.getText());
                assertEquals(0, new BigDecimal("2.0").compareTo(saved.getRating()));
            }

            @Test
            @DisplayName("작성자가 아니면 InvalidUserException이고 DB는 그대로다")
            void notOwner() {
                Review review = someReview();
                AppUser stranger = createUser();

                assertThrows(InvalidUserException.class, () -> bean(ReviewCommandService.class)
                        .editReview(stranger.getId(), review.getId(), request("해킹", "1.0")));
                assertEquals(review.getText(), findReview(review.getId()).getText());
            }

            @Test
            @DisplayName("없는 리뷰면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                assertThrows(NotFoundException.class, () -> bean(ReviewCommandService.class)
                        .editReview(user.getId(), Long.MAX_VALUE, request("x", "1.0")));
            }

            @Test
            @DisplayName("평점이 5.0을 넘으면 OutOfRangeException이고 DB는 그대로다")
            void ratingOutOfRange() {
                Review review = someReview();

                assertThrows(OutOfRangeException.class, () -> bean(ReviewCommandService.class)
                        .editReview(review.getUser().getId(), review.getId(), request("x", "5.5")));
                assertEquals(0, new BigDecimal("4.0").compareTo(findReview(review.getId()).getRating()));
            }

            @Test
            @DisplayName("[BUG] 평점 없이 텍스트만 수정할 수 있다")
            void partialUpdate() {
                // 버그: editReview가 request.getRating()이 null이면 NullPointerException을 던진다
                Review review = someReview();

                bean(ReviewCommandService.class).editReview(review.getUser().getId(), review.getId(), request("텍스트만", null));

                Review saved = findReview(review.getId());
                assertEquals("텍스트만", saved.getText());
                assertEquals(0, new BigDecimal("4.0").compareTo(saved.getRating()));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/review/{review_id}")
    @DisplayName("DELETE /review/{review_id} - 리뷰 삭제")
    class DeleteReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("작성자는 200과 result=true를 받고 이후 조회는 404")
            void deletesReview() {
                Review review = someReview();

                ApiResponse response = client.delete("/review/" + review.getId())
                        .bearer(login(review.getUser().getId())).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/review/" + review.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("작성자가 아니면 403이고 리뷰는 남아 있다")
            void notOwner() {
                Review review = someReview();
                client.delete("/review/" + review.getId()).bearer(login(createUser())).send().assertStatus(403);
                client.get("/review/" + review.getId()).send().assertStatus(200);
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.delete("/review/" + Long.MAX_VALUE).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                Review review = someReview();
                client.delete("/review/" + review.getId()).send().assertStatus(401);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathParam() {
                client.delete("/review/abc").bearer(login(createUser())).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("작성자가 삭제하면 DB에서 사라진다")
            void success() {
                Review review = someReview();

                assertTrue(bean(ReviewCommandService.class).deleteReview(review.getUser().getId(), review.getId()).getResult());
                assertEquals(Optional.empty(), bean(ReviewRepository.class).findById(review.getId()));
            }

            @Test
            @DisplayName("작성자가 아니면 InvalidUserException이고 리뷰는 남아 있다")
            void notOwner() {
                Review review = someReview();
                AppUser stranger = createUser();

                assertThrows(InvalidUserException.class, () ->
                        bean(ReviewCommandService.class).deleteReview(stranger.getId(), review.getId()));
                assertTrue(bean(ReviewRepository.class).findById(review.getId()).isPresent());
            }

            @Test
            @DisplayName("없는 리뷰면 NotFoundException")
            void notFound() {
                AppUser user = createUser();
                assertThrows(NotFoundException.class, () ->
                        bean(ReviewCommandService.class).deleteReview(user.getId(), Long.MAX_VALUE));
            }
        }
    }
}
