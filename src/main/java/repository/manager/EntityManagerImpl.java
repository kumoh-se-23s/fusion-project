package repository.manager;

import exception.QueryErrorException;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.CriteriaQuery;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.transaction.EntityTransaction;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class EntityManagerImpl implements EntityManager {
    private EntityMetadata metadata;
    private Class<?> resultClass;
    private Connection connection;

public EntityManagerImpl(Class<?> resultClass, Connection connection) {
        this.resultClass = resultClass;
        this.metadata = EntityMetadata.of(resultClass);
        this.connection = connection;
    }

    private PreparedStatement executeQuery(String sql, Object entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (metadata.getVersion().getValue(entity) == null) {
            metadata.getVersion().setValue(entity, 0L);
        }
        List<ColumnInfo> columns = metadata.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo columnInfo = columns.get(i);
            preparedStatement.setObject(i + 1, columnInfo.getValue(entity).toString());
        }
        return preparedStatement;
    }
    @Override
    public void persist(Object entity) {
        if (entity == null) {
            return;
        }
        String sql = metadata.getInsertSql();
        try (PreparedStatement preparedStatement = executeQuery(sql, entity)) {
            
            metadata.getPk().setValue(entity, preparedStatement.executeUpdate());
        } catch (SQLException e) {
            throw new QueryErrorException("Insert Fail\n" + sql, e);
        }

    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        if (primaryKey == null) {
            return null;
        }
        String sql = metadata.getSelectByPkSql();
        Object resultSet = executeQuery(sql, primaryKey);
        if (resultSet == null) {
            return null;
        }
        return resultSet

    }

    @Override
    public <T> T findWithLock(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return null;
    }

    @Override
    public <T> List<T> findBy(Class<T> entityClass, CriteriaQuery<T> criteriaQuery, Object primaryKey) {
        return List.of();
    }

    @Override
    public <T> List<T> findByWithLock(Class<T> entityClass, CriteriaQuery<T> criteriaQuery, Object primaryKey, LockModeType lockMode) {
        return List.of();
    }

    @Override
    public void remove(Object entity) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void clear() {

    }

    @Override
    public EntityTransaction getTransaction() {
        return EntityTransaction
    }

    @Override
    public void close() {

    }
}
