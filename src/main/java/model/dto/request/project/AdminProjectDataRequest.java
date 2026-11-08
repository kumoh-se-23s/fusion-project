package model.dto.request.project;

import constant.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.UpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AdminProjectDataRequest extends UpdateRequest {
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private String title;

    private String text;

    private byte[] imgFile;

    private BigDecimal donateAmount;

    private ProjectState state;

}
