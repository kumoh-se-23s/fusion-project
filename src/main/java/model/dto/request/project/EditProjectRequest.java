package model.dto.request.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditProjectRequest extends Request {
    private EditProjectDataRequest projectDataDTO;

    private Long categoryId;
}


