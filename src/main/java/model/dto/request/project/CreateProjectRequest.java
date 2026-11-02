package model.dto.request.project;

import annotation.BindDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Project;
import model.dto.request.Request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Project.class)
public class CreateProjectRequest extends Request {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private String title;

    private String text;

    private byte[] imgFile;
}
