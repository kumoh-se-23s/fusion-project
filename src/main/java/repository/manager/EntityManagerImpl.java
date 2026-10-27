package repository.manager;

import config.PooledDataSource;
import exception.LazyInitializationException;
import exception.NotFoundException;
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
import java.util.List;
import java.util.function.Function;

public class EntityManagerImpl implements EntityManager {
    private static final String FOR_UPDATE = " FOR UPDATE";

    private final EntityMapper mapper = new EntityMapper(this::createReference);

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

        List<Object> params = values(entity, metadata.getInsertColumns());
        Class<?> keyType = metadata.isAutoIncrement() ? metadata.getPk().getTargetClass() : null;
        Object key = withConnection(c -> SQLExecutor.insert(c, metadata.getInsertSql(), params, keyType));
        if (key != null) {
            metadata.getPk().setValue(entity, key);
        }
    }

    @Override
    public void merge(Object entity) {
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
        List<T> result = withConnection(c ->
                SQLExecutor.query(c, sql, List.of(primaryKey), rs -> mapper.map(rs, entityClass)));
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
        return withConnection(c ->
                SQLExecutor.query(c, sql, criteriaQuery.getParameters(), rs -> mapper.map(rs, entityClass)));
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

    //Lazy 참조 생성. 엔티티를 조회한 트랜잭션 안에서만 get()으로 로딩할 수 있다
    private <T> Lazy<T> createReference(Class<T> type, Object pk) {
        ConnectionHolder.Scope scope = ConnectionHolder.currentScope();
        return Lazy.reference(type, pk, () -> {
            if (scope == null || ConnectionHolder.currentScope() != scope) {
                throw new LazyInitializationException(
                        "Cannot load " + type.getSimpleName() + "#" + pk
                                + " outside of the transaction it was loaded in");
            }
            SqlLogger.lazy(type, pk);
            return find(type, pk);
        });
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
