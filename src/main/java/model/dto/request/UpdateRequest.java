package model.dto.request;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class UpdateRequest extends Request {
    //null이 아닌 필드만 {필드명: 값} 맵으로 변환. BaseRepository.update(PK, Map)에 사용
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
                        updateData.putIfAbsent(field.getName(), value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return updateData;
    }
}
