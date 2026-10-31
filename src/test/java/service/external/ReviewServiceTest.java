package service.external;

import example.db.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ReviewService")
public class ReviewServiceTest extends DatabaseTest {
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = db.config.getReviewService();
    }

    @Nested
    @DisplayName("createReview")
    class CreateReview {
        @Test
        @DisplayName("배송 완료된 후원자는 리뷰를 작성한다")
        void success() {
        }

        @Test
        @DisplayName("배송 전이면 NotCompleteFundingException")
        void notDelivered() {
        }

        @Test
        @DisplayName("후원하지 않았으면 NotCompleteFundingException")
        void notDonated() {
        }

        @Test
        @DisplayName("이미 작성했으면 AlreadyExistException")
        void duplicate() {
        }

        @ParameterizedTest(name = "별점 {0}")
        @ValueSource(strings = {"-0.5", "5.5"})
        @DisplayName("별점이 범위를 벗어나면 OutOfRangeException")
        void ratingOutOfRange(String rating) {
        }

        @Test
        @DisplayName("별점이 없으면 NotNullException")
        void withoutRating() {
        }

        @Test
        @DisplayName("없는 사용자면 NotFoundException")
        void userNotFound() {
        }
    }
}
