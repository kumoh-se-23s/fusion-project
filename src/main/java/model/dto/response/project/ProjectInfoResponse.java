package model.dto.response.project;

import annotation.BindDomain;
import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Project;
import model.dto.response.Response;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BindDomain(values = Project.class)
public class ProjectInfoResponse extends Response {

    @DataField(name = "id")
    private Long projectId;

    private String title;

    private BigDecimal purposeAmount;

    private BigDecimal donateAmount;

    @DataField(name = "category.id")
    private Long categoryId;
}
