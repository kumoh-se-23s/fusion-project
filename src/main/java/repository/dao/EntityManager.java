package repository.dao;

import java.util.List;
import java.util.Map;

public interface EntityManager { void persist(Object entity);

    <T> T find(Class<T> entityClass, Object primaryKey);

    void remove(Object entity);

    void flush();

    void clear();

    void close();
}
