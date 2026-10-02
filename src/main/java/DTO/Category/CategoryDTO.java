package DTO.Category;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@Getter
@Builder
public class CategoryDTO implements Serializable {
    @NonNull
    private String name;

    private String description;
}
