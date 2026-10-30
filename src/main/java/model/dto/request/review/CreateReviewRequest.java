package model.dto.request.review;

import lombok.*;
import model.dto.request.Request;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest extends Request {
    private Long rewardPK;

    private String text;

    private BigDecimal rating;

    private byte[] imgFile;
}
