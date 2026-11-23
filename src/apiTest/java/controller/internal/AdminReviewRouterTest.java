package controller.internal;

import com.fasterxml.jackson.databind.JsonNode;
import exception.NotFoundException;
import exception.OutOfRangeException;
import fixture.AdminUserFixtures;
import model.domain.AppUser;
import model.domain.Review;
import model.domain.Reward;
import model.dto.ReviewData;
import model.dto.request.review.AdminReviewRequest;
import model.dto.request.review.ReviewDataRequest;
import model.dto.response.DataCollectionResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ReviewRepository;
import service.internal.AdminReviewControlService;
import service.internal.AdminReviewQueryService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.StreamSupport;

import static fixture.AdminUserFixtures.MISSING_ID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminReviewRouter (/internal/review)")
class AdminReviewRouterTest extends ApiTest {

    private Review createReview() {
        AppUser writer = createUser();
        Reward reward = AdminUserFixtures.reward(createUser());
        return AdminUserFixtures.review(writer, reward, unique("리뷰"), "4.0");
    }

    private Review findReview(long reviewId) {
        return bean(ReviewRepository.class).findById(reviewId).orElseThrow();
    }

    private static Map<String, Object> body(long userId, long rewardId, Map<String, Object> request) {
        return Map.of("userId", userId, "rewardId", rewardId, "request", request);
    }

    private static AdminReviewRequest request(Long userId, Long rewardId, String text, String rating) {
        return AdminReviewRequest.builder()
                .userId(userId)
                .rewardId(rewardId)
                .request(ReviewDataRequest.builder()
                        .text(text)
                        .rating(rating == null ? null : new BigDecimal(rating))
                        .build())
                .build();
    }

    @Nested
    @ApiEndpoint(method = "POST", path = "/internal/review")
    @DisplayName("POST /internal/review - 관리자 리뷰 생성")
    class CreateReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("[BUG] 200과 만들어진 리뷰 id를 돌려주고 DB에 저장된다")
            void createsReview() {
                // 버그: ReviewDataRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException을 던진다 (생성이 항상 실패)
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());
                String text = unique("리뷰");

                ApiResponse response = client.post("/internal/review")
                        .json(body(writer.getId(), reward.getId(), Map.of("text", text, "rating", 4.5)))
                        .send()
                        .assertStatus(200);

                long id = response.json("/id").asLong();
                Review saved = findReview(id);
                assertEquals(text, saved.getText());
                assertEquals(0, new BigDecimal("4.5").compareTo(saved.getRating()));
                assertEquals(writer.getId(), saved.getUser().getId());
                assertEquals(reward.getId(), saved.getReward().getId());
            }

            @Test
            @DisplayName("본문이 없으면 422")
            void missingBody() {
                client.post("/internal/review").send().assertStatus(422);
            }

            @Test
            @DisplayName("userId 타입이 틀리면 422")
            void wrongFieldType() {
                client.post("/internal/review")
                        .rawJson("{\"userId\":\"abc\",\"rewardId\":1,\"request\":{\"text\":\"t\",\"rating\":3.0}}")
                        .send()
                        .assertStatus(422);
            }

            @Test
            @DisplayName("없는 유저면 404")
            void userNotFound() {
                Reward reward = AdminUserFixtures.reward(createUser());

                client.post("/internal/review")
                        .json(body(MISSING_ID, reward.getId(), Map.of("text", "t", "rating", 3.0)))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("없는 리워드면 404")
            void rewardNotFound() {
                AppUser writer = createUser();

                client.post("/internal/review")
                        .json(body(writer.getId(), MISSING_ID, Map.of("text", "t", "rating", 3.0)))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("[BUG] 평점이 5.0을 넘으면 400")
            void ratingOutOfRange() {
                // 버그: ReviewDataRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException을 던진다 (생성이 항상 실패)
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());

                client.post("/internal/review")
                        .json(body(writer.getId(), reward.getId(), Map.of("text", "t", "rating", 5.5)))
                        .send()
                        .assertStatus(400);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("[BUG] 경계값 0.0과 5.0은 저장된다")
            void boundaryRatings() {
                // 버그: ReviewDataRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException을 던진다 (생성이 항상 실패)
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());
                AdminReviewControlService service = bean(AdminReviewControlService.class);

                IdResponse low = service.createReview(request(writer.getId(), reward.getId(), "낮음", "0.0"));
                IdResponse high = service.createReview(request(createUser().getId(), reward.getId(), "높음", "5.0"));

                assertEquals(0, BigDecimal.ZERO.compareTo(findReview(low.getId()).getRating()));
                assertEquals(0, new BigDecimal("5.0").compareTo(findReview(high.getId()).getRating()));
            }

            @Test
            @DisplayName("[BUG] 평점이 0.0보다 작으면 OutOfRangeException이고 저장되지 않는다")
            void negativeRating() {
                // 버그: ReviewDataRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException을 던진다 (생성이 항상 실패)
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());

                assertThrows(OutOfRangeException.class, () -> bean(AdminReviewControlService.class)
                        .createReview(request(writer.getId(), reward.getId(), "음수", "-0.1")));
                assertTrue(bean(ReviewRepository.class).findByUser(writer.getId()).isEmpty());
            }

            @Test
            @DisplayName("없는 유저/리워드면 NotFoundException")
            void notFound() {
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());
                AdminReviewControlService service = bean(AdminReviewControlService.class);

                assertThrows(NotFoundException.class,
                        () -> service.createReview(request(MISSING_ID, reward.getId(), "t", "3.0")));
                assertThrows(NotFoundException.class,
                        () -> service.createReview(request(writer.getId(), MISSING_ID, "t", "3.0")));
            }

            @Test
            @DisplayName("[BUG] 좋아요/싫어요 수는 0으로 시작한다")
            void countersStartAtZero() {
                // 버그: ReviewDataRequest에 @BindDomain이 없어 Serializer.toDomain이 SerializeException을 던진다 (생성이 항상 실패)
                AppUser writer = createUser();
                Reward reward = AdminUserFixtures.reward(createUser());

                IdResponse result = bean(AdminReviewControlService.class)
                        .createReview(request(writer.getId(), reward.getId(), "t", "3.0"));

                Review saved = findReview(result.getId());
                assertEquals(0L, saved.getLikeCount());
                assertEquals(0L, saved.getDislikeCount());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/review/{review_id}")
    @DisplayName("GET /internal/review/{review_id} - 리뷰 단건 조회")
    class GetReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 리뷰 정보를 돌려준다")
            void returnsReview() {
                Review saved = createReview();

                ApiResponse response = client.get("/internal/review/" + saved.getId()).send().assertStatus(200);

                JsonNode data = response.json("/data");
                assertEquals(saved.getId(), data.path("id").asLong());
                assertEquals(saved.getUser().getId(), data.path("userId").asLong());
                assertEquals(saved.getReward().getId(), data.path("rewardId").asLong());
                assertEquals(saved.getText(), data.path("text").asText());
                assertEquals(0, new BigDecimal("4.0").compareTo(data.path("rating").decimalValue()), response::toString);
                assertEquals(0, data.path("likeCount").asLong());
                assertEquals(0, data.path("dislikeCount").asLong());
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.get("/internal/review/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathType() {
                client.get("/internal/review/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("도메인 값이 ReviewData로 옮겨진다")
            void mapsFields() {
                Review saved = createReview();

                ReviewData data = bean(AdminReviewQueryService.class).getReview(saved.getId()).getData();

                assertEquals(saved.getId(), data.getId());
                assertEquals(saved.getUser().getId(), data.getUserId());
                assertEquals(saved.getReward().getId(), data.getRewardId());
                assertEquals(saved.getText(), data.getText());
            }

            @Test
            @DisplayName("없는 리뷰면 NotFoundException")
            void notFound() {
                assertThrows(NotFoundException.class, () -> bean(AdminReviewQueryService.class).getReview(MISSING_ID));
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "GET", path = "/internal/review")
    @DisplayName("GET /internal/review - 리뷰 전체 조회")
    class GetAllReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 리뷰 목록, amount를 돌려준다")
            void returnsAll() {
                Review saved = createReview();

                ApiResponse response = client.get("/internal/review").send().assertStatus(200);

                JsonNode datas = response.json("/datas");
                assertTrue(datas.isArray(), response::toString);
                assertEquals(datas.size(), response.json("/amount").asLong());
                assertTrue(StreamSupport.stream(datas.spliterator(), false)
                                .anyMatch(node -> node.path("id").asLong() == saved.getId()),
                        () -> "방금 만든 리뷰가 응답에 없다: " + response);
            }

            @Test
            @DisplayName("limit을 주면 그 수 이하만 돌려준다")
            void limit() {
                createReview();
                createReview();

                ApiResponse response = client.get("/internal/review").query("limit", 1).send().assertStatus(200);

                assertEquals(1, response.json("/datas").size());
                assertEquals(1, response.json("/amount").asLong());
            }

            @Test
            @DisplayName("limit이 숫자가 아니면 422")
            void badLimitType() {
                client.get("/internal/review").query("limit", "abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("limit이 없으면 DB의 모든 리뷰를 돌려준다")
            void noLimit() {
                createReview();

                DataCollectionResponse<ReviewData> result = bean(AdminReviewQueryService.class).getAllReviews(null);

                assertEquals(bean(ReviewRepository.class).findAll().size(), result.getAmount());
                assertEquals(result.getAmount(), (long) result.getDatas().size());
            }

            @Test
            @DisplayName("limit만큼 잘라서 돌려준다")
            void withLimit() {
                createReview();
                createReview();

                DataCollectionResponse<ReviewData> result = bean(AdminReviewQueryService.class).getAllReviews(2);

                assertEquals(2L, result.getAmount());
                assertEquals(2, result.getDatas().size());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/internal/review/{review_id}")
    @DisplayName("PATCH /internal/review/{review_id} - 리뷰 수정")
    class UpdateReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 리뷰 id를 돌려주고 보낸 값이 DB에 반영된다")
            void updates() {
                Review saved = createReview();
                String text = unique("수정");

                ApiResponse response = client.patch("/internal/review/" + saved.getId())
                        .json(body(saved.getUser().getId(), saved.getReward().getId(), Map.of("text", text, "rating", 2.5)))
                        .send()
                        .assertStatus(200);

                assertEquals(saved.getId(), response.json("/id").asLong());
                Review updated = findReview(saved.getId());
                assertEquals(text, updated.getText());
                assertEquals(0, new BigDecimal("2.5").compareTo(updated.getRating()));
            }

            @Test
            @DisplayName("수정 결과가 단건 조회에도 보인다")
            void visibleOnGet() {
                Review saved = createReview();
                String text = unique("수정");

                client.patch("/internal/review/" + saved.getId())
                        .json(body(saved.getUser().getId(), saved.getReward().getId(), Map.of("text", text)))
                        .send()
                        .assertStatus(200);

                ApiResponse response = client.get("/internal/review/" + saved.getId()).send().assertStatus(200);
                assertEquals(text, response.json("/data/text").asText());
                assertEquals(0, new BigDecimal("4.0").compareTo(response.json("/data/rating").decimalValue()),
                        "보내지 않은 평점은 그대로여야 한다");
            }

            @Test
            @DisplayName("다른 리워드로 옮길 수 있다")
            void changesReward() {
                Review saved = createReview();
                Reward other = AdminUserFixtures.reward(createUser());

                client.patch("/internal/review/" + saved.getId())
                        .json(body(saved.getUser().getId(), other.getId(), Map.of("text", "옮김")))
                        .send()
                        .assertStatus(200);

                assertEquals(other.getId(), findReview(saved.getId()).getReward().getId());
            }

            @Test
            @DisplayName("[BUG] rewardId 없이 텍스트만 보내도 수정된다 (PATCH)")
            void partialWithoutReward() {
                // 버그: editReview가 rewardId를 무조건 findById(null)로 조회해서 부분 수정이 실패한다
                Review saved = createReview();
                String text = unique("수정");

                client.patch("/internal/review/" + saved.getId())
                        .json(Map.of("request", Map.of("text", text)))
                        .send()
                        .assertStatus(200);

                assertEquals(text, findReview(saved.getId()).getText());
                assertEquals(saved.getReward().getId(), findReview(saved.getId()).getReward().getId());
            }

            @Test
            @DisplayName("[BUG] 평점이 5.0을 넘으면 400")
            void ratingOutOfRange() {
                // 버그: editReview는 createReview와 달리 평점 범위를 검사하지 않는다
                Review saved = createReview();

                client.patch("/internal/review/" + saved.getId())
                        .json(body(saved.getUser().getId(), saved.getReward().getId(), Map.of("rating", 9.0)))
                        .send()
                        .assertStatus(400);

                assertEquals(0, new BigDecimal("4.0").compareTo(findReview(saved.getId()).getRating()));
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                Reward reward = AdminUserFixtures.reward(createUser());

                client.patch("/internal/review/" + MISSING_ID)
                        .json(body(createUser().getId(), reward.getId(), Map.of("text", "t")))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("없는 리워드면 404")
            void rewardNotFound() {
                Review saved = createReview();

                client.patch("/internal/review/" + saved.getId())
                        .json(body(saved.getUser().getId(), MISSING_ID, Map.of("text", "t")))
                        .send()
                        .assertStatus(404);
            }

            @Test
            @DisplayName("본문이 없으면 422")
            void missingBody() {
                Review saved = createReview();

                client.patch("/internal/review/" + saved.getId()).send().assertStatus(422);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathType() {
                client.patch("/internal/review/abc")
                        .json(body(1, 1, Map.of("text", "t")))
                        .send()
                        .assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("보낸 값만 바뀌고 나머지는 그대로다")
            void updatesOnlySentFields() {
                Review saved = createReview();

                IdResponse result = bean(AdminReviewControlService.class).editReview(saved.getId(),
                        request(saved.getUser().getId(), saved.getReward().getId(), null, "1.5"));

                assertEquals(saved.getId(), result.getId());
                Review updated = findReview(saved.getId());
                assertEquals(saved.getText(), updated.getText());
                assertEquals(0, new BigDecimal("1.5").compareTo(updated.getRating()));
            }

            @Test
            @DisplayName("없는 리뷰/리워드면 NotFoundException")
            void notFound() {
                Review saved = createReview();
                AdminReviewControlService service = bean(AdminReviewControlService.class);

                assertThrows(NotFoundException.class, () -> service.editReview(MISSING_ID,
                        request(saved.getUser().getId(), saved.getReward().getId(), "t", null)));
                assertThrows(NotFoundException.class, () -> service.editReview(saved.getId(),
                        request(saved.getUser().getId(), MISSING_ID, "t", null)));
                assertEquals(saved.getText(), findReview(saved.getId()).getText());
            }
        }
    }

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/internal/review/{review_id}")
    @DisplayName("DELETE /internal/review/{review_id} - 리뷰 삭제")
    class DeleteReview {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("200과 result=true를 돌려주고 이후 조회는 404")
            void deletes() {
                Review saved = createReview();

                ApiResponse response = client.delete("/internal/review/" + saved.getId()).send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                client.get("/internal/review/" + saved.getId()).send().assertStatus(404);
            }

            @Test
            @DisplayName("없는 리뷰면 404")
            void notFound() {
                client.delete("/internal/review/" + MISSING_ID).send().assertStatus(404);
            }

            @Test
            @DisplayName("review_id가 숫자가 아니면 422")
            void badPathType() {
                client.delete("/internal/review/abc").send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("삭제하면 DB에서 사라진다")
            void removesRow() {
                Review saved = createReview();

                assertTrue(bean(AdminReviewControlService.class).deleteReview(saved.getId()).getResult());
                assertFalse(bean(ReviewRepository.class).isExistById(saved.getId()));
            }

            @Test
            @DisplayName("두 번 삭제하면 두 번째는 NotFoundException")
            void deleteTwice() {
                Review saved = createReview();
                AdminReviewControlService service = bean(AdminReviewControlService.class);

                service.deleteReview(saved.getId());
                assertThrows(NotFoundException.class, () -> service.deleteReview(saved.getId()));
            }
        }
    }
}
