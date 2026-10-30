package model.dto;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Category;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Category.class)
public class CategoryData extends BaseData {
    private Long pk;

    private String name;

    private String description;
}
