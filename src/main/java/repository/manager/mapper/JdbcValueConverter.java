package repository.manager.mapper;

import annotation.Table;
import repository.manager.query_dsl.EntityMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 자바 값 <-> JDBC 값 변환
 * enum은 name()으로 저장하고, 연관 엔티티는 PK로 저장한다.
 */
public final class JdbcValueConverter {

    private JdbcValueConverter() {
    }

    //PreparedStatement에 바인딩할 값으로 변환
    public static Object toJdbc(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        if (value.getClass().isAnnotationPresent(Table.class)) {
            return EntityMetadata.of(value.getClass()).getPk().getValue(value);
        }
        return value;
    }

    //ResultSet의 컬럼 값을 필드 타입으로 읽음
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object fromJdbc(ResultSet rs, String column, Class<?> type) throws SQLException {
        if (type == byte[].class) {
            return rs.getBytes(column);
        }
        if (type.isEnum()) {
            String name = rs.getString(column);
            return name == null ? null : Enum.valueOf((Class<? extends Enum>) type, name);
        }
        return rs.getObject(column, type);
    }
}
