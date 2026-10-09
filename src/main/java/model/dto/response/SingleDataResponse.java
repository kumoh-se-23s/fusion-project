package model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dto.BaseData;
import model.dto.DataObject;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SingleDataResponse<D extends DataObject> extends Response {
    private D data;
}