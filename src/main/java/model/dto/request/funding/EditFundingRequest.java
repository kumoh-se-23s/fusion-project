package model.dto.request.funding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.request.UpdateRequest;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditFundingRequest extends UpdateRequest {
    private String title;

    private String text;

    private byte[] imgFile;

    private List<Long> categoryPKs;
}


