package model.dto.category;


import Annotation.*;
import model.dto.BaseDTO;
import model.domain.Category;
import lombok.*;

@Getter
@Builder
@BindDomain(Category.class)
@AllArgsConstructor
@NoArgsConstructor
public class RequestCategoryDTO extends BaseDTO {
    @NotNull
    @DomainField("name")
    private String name;

    @DomainField("description")
    private String description;
    
}
