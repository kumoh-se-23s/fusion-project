package model.dto.request.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest extends Request {
    private String name;

    private String description;
}
