package repository.manager;

import config.PooledDataSource;
import exception.NotFoundException;
import exception.NotNullException;
import exception.OptimisticLockException;
import exception.TransactionException;
import repository.manager.mapper.EntityMapper;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.CriteriaQuery;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.transaction.ConnectionHolder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class EntityManagerImpl implements EntityManager {
    private static final String FOR_UPDATE = " FOR UPDATE";
    //연관 엔티티 IN 조회 시 한 쿼리에 넣는 최대 PK 개수
    private static final int BATCH_SIZE = 1000;

    private final EntityMapper mapper = new EntityMapper();

    @Override
    public void persist(Object entity) {
        if (entity == null) {
            return;
        }
        EntityMetadata metadata = EntityMetadata.of(entity.getClass());
        ColumnInfo version = metadata.getVersion();
        if (version != null && version.getValue(entity) == null) {
            version.setValue(entity, 0L);
        }
        //NOT NULL 컬럼에 null을 넣으면 DB 오류 대신 400으로 응답
        for (ColumnInfo column : metadata.getInsertColumns()) {
            if (!column.isNullable() && column.getValue(entity) == null) {
                throw new NotNullException("'" + column.field().getName() + "' cannot be null");
            }
        }

        List<Object> params = values(entity, metadata.getInsertColumns());
        Class<?> keyType = metadata.isAutoIncrement() ? metadata.getPk().getTargetClass() : null;
        Object key = withConnection(c -> SQLExecutor.insert(c, metadata.getInsertSql(), params, keyType));
        if (key != null) {
            metadata.getPk().setValue(entity, key);
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return findWithLock(entityClass, primaryKey, LockModeType.NONE);
    }

    @Override
    public <T> T findWithLock(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        if (primaryKey == null) {
            return null;
        }
        String sql = withLock(EntityMetadata.of(entityClass).getSelectByPkSql(), lockMode);
        List<T> result = withConnection(c -> load(c, sql, List.of(primaryKey), entityClass, new HashMap<>()));
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public <T> List<T> findBy(CriteriaQuery<T> criteriaQuery) {
        return findByWithLock(criteriaQuery, LockModeType.NONE);
    }

    @Override
    public <T> List<T> findByWithLock(CriteriaQuery<T> criteriaQuery, LockModeType lockMode) {
        Class<T> entityClass = criteriaQuery.getResultClass();
        String sql = withLock(criteriaQuery.toSql(), lockMode);
        return withConnection(c -> load(c, sql, criteriaQuery.getParameters(), entityClass, new HashMap<>()));
    }

    @Override
    public void merge(Object entity) {
        EntityMetadata metadata = EntityMetadata.of(entity.getClass());
        ColumnInfo version = metadata.getVersion();
        Object pk = metadata.getPk().getValue(entity);

        List<Object> params = values(entity, metadata.getUpdatableColumns());
        params.add(pk);
        if (version != null) {
            params.add(version.getValue(entity));
        }

        int rows = withConnection(c -> SQLExecutor.update(c, metadata.getUpdateSql(), params));
        if (rows == 0) {
            if (version != null) {
                throw new OptimisticLockException(
                        entity.getClass().getSimpleName() + "#" + pk + " was modified or deleted by another transaction");
            }
            throw new NotFoundException(entity.getClass().getSimpleName() + "#" + pk + " not found");
        }
        if (version != null) {
            version.setValue(entity, (Long) version.getValue(entity) + 1);
        }
    }

    @Override
    public boolean remove(Class<?> entityClass, Object primaryKey) {
        if (primaryKey == null) {
            return false;
        }
        String sql = EntityMetadata.of(entityClass).getDeleteSql();
        return withConnection(c -> SQLExecutor.update(c, sql, List.of(primaryKey))) > 0;
    }

    //조회 후 연관 엔티티를 같은 커넥션으로 즉시 로딩
    //loaded : 한 번의 조회 안에서 같은 엔티티를 다시 조회하지 않기 위한 캐시 (키: [타입, PK])
    private <T> List<T> load(Connection c, String sql, List<?> params, Class<T> entityClass,
                             Map<List<Object>, Object> loaded) {
        List<T> entities = SQLExecutor.query(c, sql, params, rs -> mapper.map(rs, entityClass));
        for (T entity : entities) {
            loaded.putIfAbsent(cacheKey(entity), entity);
        }
        loadRelations(c, entityClass, entities, loaded);
        return entities;
    }

    //매퍼가 넣어둔 PK만 있는 연관 엔티티를 실제 엔티티로 교체
    //엔티티마다 조회하지 않고(N+1), 연관 타입별로 PK를 모아 IN 쿼리로 한 번에 조회한다
    private void loadRelations(Connection c, Class<?> entityClass, List<?> entities, Map<List<Object>, Object> loaded) {
        if (entities.isEmpty()) {
            return;
        }
        List<ColumnInfo> relations = EntityMetadata.of(entityClass).getColumns().stream()
                .filter(ColumnInfo::isRelation)
                .toList();

        //아직 불러오지 않은 연관 엔티티의 PK를 타입별로 수집
        Map<Class<?>, Set<Object>> missing = new LinkedHashMap<>();
        for (ColumnInfo column : relations) {
            for (Object entity : entities) {
                Object stub = column.getValue(entity);
                if (stub != null && !loaded.containsKey(cacheKey(stub))) {
                    Object pk = EntityMetadata.of(stub.getClass()).getPk().getValue(stub);
                    missing.computeIfAbsent(column.getRelationClass(), k -> new LinkedHashSet<>()).add(pk);
                }
            }
        }
        missing.forEach((type, pks) -> loadByPks(c, type, new ArrayList<>(pks), loaded));

        for (ColumnInfo column : relations) {
            for (Object entity : entities) {
                Object stub = column.getValue(entity);
                if (stub == null) {
                    continue;
                }
                Object found = loaded.get(cacheKey(stub));
                if (found == null) {
                    throw new NotFoundException(stub.getClass().getSimpleName() + "#"
                            + EntityMetadata.of(stub.getClass()).getPk().getValue(stub) + " referenced by "
                            + entityClass.getSimpleName() + "." + column.field().getName() + " not found");
                }
                column.setValue(entity, found);
            }
        }
    }

    //PK 목록을 BATCH_SIZE씩 나눠 IN 쿼리로 조회. 조회된 엔티티의 연관도 같은 방식으로 로딩된다
    private void loadByPks(Connection c, Class<?> type, List<Object> pks, Map<List<Object>, Object> loaded) {
        EntityMetadata metadata = EntityMetadata.of(type);
        for (int from = 0; from < pks.size(); from += BATCH_SIZE) {
            List<Object> chunk = pks.subList(from, Math.min(from + BATCH_SIZE, pks.size()));
            load(c, metadata.getSelectByPksSql(chunk.size()), chunk, type, loaded);
        }
    }

    private static List<Object> cacheKey(Object entity) {
        return List.of(entity.getClass(), EntityMetadata.of(entity.getClass()).getPk().getValue(entity));
    }

    private String withLock(String sql, LockModeType lockMode) {
        if (lockMode != LockModeType.PESSIMISTIC_WRITE) {
            return sql; //OPTIMISTIC은 merge 시 @Version으로 검사
        }
        if (!ConnectionHolder.isBound()) {
            throw new TransactionException("PESSIMISTIC_WRITE requires an active transaction");
        }
        return sql + FOR_UPDATE;
    }

    private static List<Object> values(Object entity, List<ColumnInfo> columns) {
        List<Object> values = new ArrayList<>(columns.size());
        for (ColumnInfo column : columns) {
            values.add(column.getValue(entity));
        }
        return values;
    }

    //트랜잭션 커넥션이 있으면 사용, 없으면 이 작업만을 위한 커넥션을 열고 닫음
    private static <R> R withConnection(Function<Connection, R> work) {
        Connection bound = ConnectionHolder.get();
        if (bound != null) {
            return work.apply(bound);
        }
        try (Connection connection = PooledDataSource.getDataSource().getConnection()) {
            return work.apply(connection);
        } catch (SQLException e) {
            throw new TransactionException("Failed to get connection", e);
        }
    }
}
