package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "PROJECT_CATEGORY")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCategory extends BaseDomain {
    @Id
    @Column(name = "project_category_id")
    private Long id;

    @JoinColumn(name = "category_fk", nullable = false, uniqueGroup = "project_category", onDelete = OnDelete.CASCADE)
    private Category category;

    @JoinColumn(name = "project_fk", nullable = false, uniqueGroup = "project_category", onDelete = OnDelete.CASCADE)
    private Project project;
}
