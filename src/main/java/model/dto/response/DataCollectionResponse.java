package model.dto.response;

import lombok.*;
import model.dto.BaseData;

import java.util.Collection;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataCollectionResponse<D extends BaseData> extends Response {
    private Long amount;
    private Collection<D> datas;
}
