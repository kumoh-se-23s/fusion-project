package model.domain;

import annotation.Column;
import annotation.JoinColumn;
import annotation.OnDelete;
import annotation.Id;
import annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "DENY_PROJECT")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DenyProject extends BaseDomain {
    @Id
    @Column(name = "deny_project_id")
    private Long id;

    @JoinColumn(name = "project_fk", nullable = false, onDelete = OnDelete.CASCADE)
    private Project project;

    @Column(name = "deny_reason", nullable = false, length = 500)
    private String reason;
}
