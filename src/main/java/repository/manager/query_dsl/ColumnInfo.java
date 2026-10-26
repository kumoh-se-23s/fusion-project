package repository.manager.query_dsl;

import java.lang.reflect.Field;

/**
 * DB에 대응되는 단일 필드의 정보를 가지는 클래스
 * field : 필드
 * columnName : 필드 명
 * isRelation : 다른 도메인과 관계를 가지는지
 */

public record ColumnInfo(Field field, String columnName, boolean isRelation) {
    public Class<?> getTargetClass() {
        return field.getType();
    }

    public Object getValue(Object entity) { // 필드 getter
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read " + field, e);
        }
    }

    public void setValue(Object entity, Object value) {   //필드 setter
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot write " + field, e);
        }
    }
}