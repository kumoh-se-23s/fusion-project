package model.dto.request.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectByCategoryRequest extends Request {
    private CreateProjectRequest createProjectRequest;
    private List<Long> categoryIds;
}
