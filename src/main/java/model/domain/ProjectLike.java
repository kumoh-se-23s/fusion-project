package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "PROJECT_LIKE")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLike extends BaseDomain {
    @Id
    @Column(name = "project_like_id")
    private Long id;

    @JoinColumn(name = "project_fk", nullable = false, uniqueGroup = "like", onDelete = OnDelete.CASCADE)
    private Project project;

    @JoinColumn(name = "user_fk", nullable = false, uniqueGroup = "like", onDelete = OnDelete.CASCADE)
    private AppUser user;

    @Builder.Default
    @Column(name = "like_type", nullable = false)
    private Boolean likeType = true;
}
