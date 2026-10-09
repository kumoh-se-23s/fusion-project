package model.dto.response;

import model.dto.DataObject;

import java.util.Collection;

public class DataCollectionResponse<D extends DataObject> {
    private Long amount;
    private Collection<D> datas;
}
