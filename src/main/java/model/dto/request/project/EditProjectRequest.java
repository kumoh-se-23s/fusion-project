package model.dto.request.project;

import annotation.DataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.Request;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditProjectRequest extends Request {
    private String title;

    private String text;

    private byte[] imgFile;

    @DataField(name = "category.id")
    private Long categoryId;
}


