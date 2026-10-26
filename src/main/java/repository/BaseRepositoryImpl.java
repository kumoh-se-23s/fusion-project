package repository;

import model.domain.BaseDomain;

import java.util.List;
import java.util.Optional;

public abstract class BaseRepositoryImpl<T extends BaseDomain, ID> implements BaseRepository<T, ID> {
    @Override
    public T save(T entity) {
        return null;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        return List.of();
    }

    @Override
    public List<T> find(Filter filter) {
        return List.of();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public boolean delete(T entity) {
        return false;
    }
}