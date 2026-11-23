package model.dto.request.review;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Review;
import model.dto.request.UpdateRequest;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Review.class)
public class ReviewDataRequest extends UpdateRequest {
    private String text;

    private BigDecimal rating;

    private byte[] imgFile;
}
