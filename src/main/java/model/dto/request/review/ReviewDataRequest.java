package model.dto.request.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.UpdateRequest;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDataRequest extends UpdateRequest {
    private String text;

    private BigDecimal rating;

    private byte[] imgFile;
}
