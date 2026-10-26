package repository;

import model.domain.BaseDomain;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class BaseRepositoryImpl<T extends BaseDomain, PK> implements BaseRepository<T, PK> {
    @Override
    public T create(T entity) {
        return null;
    }

    @Override
    public List<T> createAll(List<T> entities) {
        return List.of();
    }

    @Override
    public List<T> find(Filter filter) {
        return List.of();
    }

    @Override
    public Optional<T> findById(PK pk) {
        return Optional.empty();
    }

    @Override
    public Optional<T> update(PK pk, Map<String, Object> updateData) {
        return Optional.empty();
    }

    @Override
    public boolean delete(PK pk) {
        return false;
    }
}