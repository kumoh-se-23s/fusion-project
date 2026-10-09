package model.dto.request.funding;

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
public class EditFundingRequest extends Request {
    private String text;

    private Byte[] imgFile;

    private List<Long> categoryPKs;
}


