package model.dto.response.projectCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.IdResponse;
import model.dto.response.Response;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindCategoryResponse extends Response {
    private List<IdResponse> projectCategoryIds;
}
