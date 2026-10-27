package model.domain;

import annotation.Column;
import annotation.PK;
import annotation.Table;
import lombok.*;

@Table(name = "CATEGORY")
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseDomain {
    @PK
    @Column(name = "category_pk")
    private Long pk;

    @NonNull
    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "category_description")
    private String description;
}
