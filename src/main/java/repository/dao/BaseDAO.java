package repository.dao;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    Optional<T> get(Long id);
    List<T> getAll();

    Long save(T t);

    Long update(T t, String[] params);

    Boolean delete(T t);

}
