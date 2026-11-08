package model.dto.request.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.UpdateRequest;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LikeRequest extends UpdateRequest {
    private Boolean likeType;
}
