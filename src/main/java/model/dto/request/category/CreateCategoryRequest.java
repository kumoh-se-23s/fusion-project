package model.dto.request.category;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Category;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Category.class)
public class CreateCategoryRequest extends Request {
    private String name;

    private String description;
}
