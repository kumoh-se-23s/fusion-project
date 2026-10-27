package repository.manager.mapper;

import exception.QueryErrorException;
import repository.manager.Lazy;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.EntityMetadata;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * ResultSet의 한 행을 엔티티로 변환하는 클래스
 * 기본 생성자로 만든 뒤 리플렉션으로 필드를 채운다. (빌더를 거치지 않아 @NonNull 검사와 무관)
 * 연관 컬럼은 FK 값으로 Lazy 참조를 만들어 넣는다.
 */
public class EntityMapper {
    //(연관 엔티티 타입, FK 값) -> Lazy 참조
    private final BiFunction<Class<?>, Object, Lazy<?>> referenceFactory;

    public EntityMapper(BiFunction<Class<?>, Object, Lazy<?>> referenceFactory) {
        this.referenceFactory = referenceFactory;
    }

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
        Class<?> relationClass = column.getRelationClass();
        Class<?> fkType = EntityMetadata.of(relationClass).getPk().getTargetClass();
        Object fk = rs.getObject(column.columnName(), fkType);
        return fk == null ? null : referenceFactory.apply(relationClass, fk);
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
