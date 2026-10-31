package repository.manager.query_dsl;

import annotation.Column;
import annotation.JoinColumn;

import java.lang.reflect.Field;

/**
 * DB에 대응되는 단일 필드의 정보를 가지는 클래스
 * field : 필드
 * columnName : 필드 명
 * isRelation : 다른 도메인과 관계를 가지는지 (필드 타입은 연관 엔티티)
 */

public record ColumnInfo(Field field, String columnName, boolean isRelation) {
    public Class<?> getTargetClass() {
        return field.getType();
    }

    //연관 엔티티 타입. 연관 컬럼이 아니면 null
    public Class<?> getRelationClass() {
        return isRelation ? field.getType() : null;
    }

    //@Column/@JoinColumn의 nullable 값. false면 DB 컬럼도 NOT NULL
    public boolean isNullable() {
        return isRelation
                ? field.getAnnotation(JoinColumn.class).nullable()
                : field.getAnnotation(Column.class).nullable();
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
