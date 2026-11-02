package model.dto.request.project;


import constant.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreateProjectRequest extends Request {
    private Long makerId;

    private CreateProjectRequest request;

    private BigDecimal donateAmount;

    private ProjectState state;

}