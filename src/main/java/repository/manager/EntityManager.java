package repository.manager;

import repository.manager.query_dsl.CriteriaQuery;
import repository.manager.transaction.EntityTransaction;

import java.util.List;

/**
 * 도메인 단위의 기본 CRUD를 제공하는 EntityManager 인터페이스
 */

public interface EntityManager extends AutoCloseable {
    void persist(Object entity);

    <T> T find(Class<T> entityClass, Object primaryKey);

    <T> T findWithLock(Class<T> entityClass, Object primaryKey, LockModeType lockMode);

    <T> List<T> findBy(Class<T> entityClass, CriteriaQuery<T> criteriaQuery, Object primaryKey);

    <T> List<T> findByWithLock(Class<T> entityClass, CriteriaQuery<T> criteriaQuery, Object primaryKey, LockModeType lockMode);

    void remove(Object entity);

    void flush();

    void clear();

    EntityTransaction getTransaction();

    @Override
    void close();
}
