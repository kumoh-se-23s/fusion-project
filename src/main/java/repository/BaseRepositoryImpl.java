package repository;

import constant.OrderDirection;
import exception.QueryErrorException;
import model.domain.BaseDomain;
import repository.manager.EntityManager;
import repository.manager.EntityManagerImpl;
import repository.manager.Lazy;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.CriteriaQuery;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.transaction.TransactionTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class BaseRepositoryImpl<T extends BaseDomain, PK> implements BaseRepository<T, PK> {
    protected final EntityManager entityManager = new EntityManagerImpl();
    private final TransactionTemplate transactionTemplate = new TransactionTemplate();
    protected final Class<T> entityClass = resolveEntityClass();
    private final EntityMetadata metadata = EntityMetadata.of(entityClass);

    @Override
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    //하나라도 실패하면 전부 롤백
    @Override
    public List<T> createAll(List<T> entities) {
        return transactionTemplate.execute(() -> {
            List<T> created = new ArrayList<>(entities.size());
            for (T entity : entities) {
                created.add(create(entity));
            }
            return created;
        });
    }

    @Override
    public List<T> find(Filter filter) {
        return entityManager.findBy(toCriteria(filter));
    }

    @Override
    public Optional<T> findById(PK pk) {
        return Optional.ofNullable(entityManager.find(entityClass, pk));
    }

    //updateData의 키는 도메인 필드명. PK와 버전은 수정할 수 없다
    @Override
    public Optional<T> update(PK pk, Map<String, Object> updateData) {
        return transactionTemplate.execute(() -> {
            T entity = entityManager.find(entityClass, pk);
            if (entity == null) {
                return Optional.empty();
            }
            updateData.forEach((field, value) -> applyField(entity, field, value));
            entityManager.merge(entity);
            return Optional.of(entity);
        });
    }

    @Override
    public boolean delete(PK pk) {
        return entityManager.remove(entityClass, pk);
    }

    private void applyField(T entity, String field, Object value) {
        ColumnInfo column = metadata.getColumnByField(field);
        if (column == metadata.getPk() || column == metadata.getVersion()) {
            throw new QueryErrorException("'" + field + "' cannot be updated");
        }
        Object fieldValue = (column.isRelation() && value != null && !(value instanceof Lazy<?>))
                ? Lazy.of(value)
                : value;
        try {
            column.setValue(entity, fieldValue);
        } catch (IllegalArgumentException e) {
            throw new QueryErrorException("Invalid value type for '" + field + "': " + value, e);
        }
    }

    //필드명 기반 Filter -> 컬럼명 기반 CriteriaQuery. 메타데이터에 없는 필드는 거부된다
    private CriteriaQuery<T> toCriteria(Filter filter) {
        CriteriaQuery<T> query = new CriteriaQuery<>(entityClass);
        if (filter == null) {
            return query;
        }
        filter.getConditions().forEach((field, value) ->
                query.whereEqual(metadata.getColumnByField(field).columnName(), value));

        for (Sort.Order order : filter.getSort().getOrders()) {
            String column = metadata.getColumnByField(order.field()).columnName();
            if (order.direction() == OrderDirection.DESC) {
                query.orderByDesc(column);
            } else {
                query.orderByAsc(column);
            }
        }
        if (filter.getLimit() != null) {
            query.limit(filter.getLimit());
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }
}
