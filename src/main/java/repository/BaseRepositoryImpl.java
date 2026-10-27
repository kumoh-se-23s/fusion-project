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


}