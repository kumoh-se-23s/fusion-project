package DTO.Category;


import Annotation.*;
import DTO.BaseDTO;
import Domain.Category;
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

    public static void main(String[] args) {

    }
}
