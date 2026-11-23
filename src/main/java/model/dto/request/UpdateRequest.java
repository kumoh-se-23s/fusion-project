package model.dto.request;

import annotation.DataField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class UpdateRequest extends Request {
    //null이 아닌 필드만 {컬럼명: 값} 맵으로 변환. BaseRepository.update(ID, Map)에 사용
    //@DataField가 있으면 name 값을, 없으면 필드명을 키로 사용
    public Map<String, Object> toUpdateMap() {
        Map<String, Object> updateData = new HashMap<>();
        try {
            for (Class<?> type = getClass(); type != UpdateRequest.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value != null) {
                        updateData.putIfAbsent(resolveKey(field), value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return updateData;
    }

    private String resolveKey(Field field) {
        DataField dataField = field.getAnnotation(DataField.class);
        if (dataField != null && !dataField.name().isEmpty()) {
            return dataField.name();
        }
        return field.getName();
    }
}