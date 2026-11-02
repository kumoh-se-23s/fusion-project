package model.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.response.IdResponse;
import model.dto.response.Response;
import model.dto.response.projectCategory.BindCategoryResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectByCategoryResponse extends Response {
    private Long id;
    private BindCategoryResponse projectCategoryIds;
}
