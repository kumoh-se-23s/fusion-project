package service.external;

import example.db.DatabaseTest;
import model.domain.Review;
import model.dto.request.review.CreateReviewRequest;
import model.dto.response.PKResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.Filter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ReviewService")
public class ReviewServiceTest extends DatabaseTest {
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = db.config.getReviewService();
    }

    @Test
    @DisplayName("배송 완료된 후원자가 리뷰를 작성한다")
    void createReview() {
        List<Review> list = db.config.getReviewRepository().find(
                Filter.builder()
                        .conditions(Map.of("reward", db.maker1CompleteReward1.getPk()))
                        .build()
        );
        System.out.println(list.size());
        for (Review review : list) {
            db.config.getReviewRepository().delete(review.getPk());
        }

        PKResponse response = reviewService.createReview(CreateReviewRequest.builder()
                .rewardPK(db.maker1CompleteReward1.getPk())
                .text("리뷰 테스트 데이터")
                .rating(new BigDecimal("1.0"))
                .build(), db.donateUser1.getPk());

        Review saved = db.config.getReviewRepository().findByPK(response.getPk()).orElseThrow();
        assertEquals(db.maker1CompleteReward1.getPk(), saved.getReward().getPk());
        assertEquals(db.donateUser1.getPk(), saved.getUser().getPk());
        assertEquals("리뷰 테스트 데이터", saved.getText());
        assertAmount("1.0", saved.getRating());


    }
}
