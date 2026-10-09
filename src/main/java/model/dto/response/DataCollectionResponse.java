package model.dto.response;

import lombok.*;
import model.dto.DataObject;

import java.util.Collection;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataCollectionResponse<D extends DataObject> extends Response {
    private Long amount;
    private Collection<D> datas;
}
