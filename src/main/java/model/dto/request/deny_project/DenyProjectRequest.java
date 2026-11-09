package model.dto.request.deny_project;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.DenyProject;
import model.dto.request.UpdateRequest;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = DenyProject.class)
public class DenyProjectRequest extends UpdateRequest {
    @DataField(name = "reason")
    private String denyReason;
}
