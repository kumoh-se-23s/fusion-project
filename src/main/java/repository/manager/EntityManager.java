package repository.manager;

import repository.manager.EntityTransaction;

public interface EntityManager extends AutoCloseable {
    void persist(Object entity);

    <T> T find(Class<T> entityClass, Object primaryKey);

    <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode);

    void remove(Object entity);

    void flush();

    void clear();

    EntityTransaction getTransaction();

    @Override
    void close();
}
