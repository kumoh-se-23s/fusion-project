package model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseData;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteResponse extends Response {
    private Boolean result = false;
}
