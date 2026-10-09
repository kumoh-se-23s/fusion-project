package model.dto.request.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLikeRequest extends Request {
    private Boolean likeType;
}
