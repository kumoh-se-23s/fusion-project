package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.DenyProject;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = DenyProject.class)
public class DenyProjectData extends BaseData {
    private Long id;

    @DataField(name = "project.id")
    private Long projectId;

    @DataField(name = "reason")
    private String denyReason;
}
