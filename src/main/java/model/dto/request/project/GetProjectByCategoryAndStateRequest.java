package model.dto.request.project;

import constant.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectByCategoryAndStateRequest extends Request {
    private List<String> categoryList;
    private List<ProjectState> projectStateList;
}
