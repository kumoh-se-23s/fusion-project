package model.dto.request.project;


import annotation.DataField;
import lombok.*;
import model.dto.request.Request;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProjectRequest extends Request {
    @DataField(name = "maker.id")
    private Long makerId;

    @DataField(name = "category.id")
    private Long categoryId;

    private AdminProjectDataRequest request;
}