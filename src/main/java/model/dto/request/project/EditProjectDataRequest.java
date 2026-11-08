package model.dto.request.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.UpdateRequest;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditProjectDataRequest extends UpdateRequest {
    private String title;

    private String text;

    private byte[] imgFile;
}
