package repository.manager.mapper;

import exception.QueryErrorException;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.EntityMetadata;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultSet의 한 행을 엔티티로 변환하는 클래스
 * 기본 생성자로 만든 뒤 리플렉션으로 필드를 채운다. (빌더를 거치지 않아 @NonNull 검사와 무관)
 * 연관 컬럼에는 PK만 채운 빈 엔티티를 넣는다. 실제 엔티티는 EntityManager가 즉시 로딩해서 교체한다.
 */
public class EntityMapper {

    public <T> T map(ResultSet rs, Class<T> entityClass) throws SQLException {
        EntityMetadata metadata = EntityMetadata.of(entityClass);
        T entity = newInstance(entityClass);

        List<ColumnInfo> all = new ArrayList<>();
        all.add(metadata.getPk());
        all.addAll(metadata.getColumns());

        for (ColumnInfo column : all) {
            column.setValue(entity, readValue(rs, column));
        }
        return entity;
    }

    private Object readValue(ResultSet rs, ColumnInfo column) throws SQLException {
        if (!column.isRelation()) {
            return JdbcValueConverter.fromJdbc(rs, column.columnName(), column.getTargetClass());
        }
        ColumnInfo relationPk = EntityMetadata.of(column.getRelationClass()).getPk();
        Object fk = rs.getObject(column.columnName(), relationPk.getTargetClass());
        if (fk == null) {
            return null;
        }
        Object stub = newInstance(column.getRelationClass());
        relationPk.setValue(stub, fk);
        return stub;
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new QueryErrorException("Cannot create " + type.getName() + " (no-args constructor required)", e);
        }
    }
}
