package DTO.Category;

import Annotation.BindModel;
import Annotation.NotNull;
import DTO.ModelSerializer;
import Domain.Category;
import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@BindModel(Category.class)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CategoryDTO implements Serializable {
    @NotNull
    private String name;

    private String description;

    public static void main(String[] args) {
        CategoryDTO dto = new CategoryDTO("test","1");
        ModelSerializer<Category> serializer = new ModelSerializer<>(Category.class);
        Category model = serializer.toEntity(dto);


    }
}
