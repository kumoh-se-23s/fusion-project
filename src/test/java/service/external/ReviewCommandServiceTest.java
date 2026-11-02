package service.external;

import example.db.DatabaseTest;
import model.domain.Review;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.Filter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ReviewCommandService")
public class ReviewCommandServiceTest extends DatabaseTest {
    private ReviewCommandService reviewCommandService;

    @BeforeEach
    void setUp() {
        reviewCommandService = db.config.getReviewCommandService();
    }

    @Test
    @DisplayName("배송 완료된 후원자가 리뷰를 작성한다")
    void createReview() {
        List<Review> list = db.config.getReviewRepository().find(
                Filter.builder()
                        .conditions(Map.of("reward", db.maker1CompleteReward1.getId()))
                        .build()
        );
        for (Review review : list) {
            db.config.getReviewRepository().delete(review.getId());
        }

        IdResponse response = reviewCommandService.createReview(CreateReviewRequest.builder()
                .rewardId(db.maker1CompleteReward1.getId())
                .text("리뷰 테스트 데이터")
                .rating(new BigDecimal("1.0"))
                .build(), db.donateUser1.getId());

        Review saved = db.config.getReviewRepository().findById(response.getId()).orElseThrow();
        assertEquals(db.maker1CompleteReward1.getId(), saved.getReward().getId());
        assertEquals(db.donateUser1.getId(), saved.getUser().getId());
        assertEquals("리뷰 테스트 데이터", saved.getText());
        assertAmount("1.0", saved.getRating());


    }
}
