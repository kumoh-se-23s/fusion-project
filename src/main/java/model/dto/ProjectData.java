package model.dto;

import annotation.BindDomain;
import annotation.DataField;
import constant.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.domain.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@BindDomain(values = Project.class)
public class ProjectData extends BaseData {
    private Long id;

    @DataField(name = "maker.id")
    private Long makerId;

    @DataField(name = "category.id")
    private Long categoryId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long likeCount;

    private Long dislikeCount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal purposeAmount;

    private BigDecimal donateAmount;

    private ProjectState state;

    private String title;

    private String text;

    private byte[] imgFile;
}
