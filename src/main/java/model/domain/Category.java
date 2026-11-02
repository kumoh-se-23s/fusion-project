package model.domain;

import annotation.Column;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "CATEGORY")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseDomain {
    @Id
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "category_description")
    private String description;
}
