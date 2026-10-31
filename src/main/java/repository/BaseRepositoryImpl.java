package repository;

import constant.OrderDirection;
import exception.NotFoundException;
import exception.QueryErrorException;
import model.domain.BaseDomain;
import repository.manager.EntityManager;
import repository.manager.EntityManagerImpl;
import repository.manager.query_dsl.ColumnInfo;
import repository.manager.query_dsl.CriteriaQuery;
import repository.manager.query_dsl.EntityMetadata;
import repository.manager.transaction.TransactionTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
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
    public List<T> findAll() {
        return entityManager.findBy(toCriteria(null));
    }
    @Override
    public Optional<T> findByPK(PK pk) {
        return Optional.ofNullable(entityManager.find(entityClass, pk));
    }

    //updateData의 키는 도메인 필드명. PK와 버전은 수정할 수 없다
    //연관 필드의 값으로는 PK, 엔티티 모두 사용 가능
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

    //filter가 null이면 조건 없이 행이 하나라도 있는지 확인 (find(null)과 동일한 의미)
    @Override
    public boolean isExist(Filter filter) {
        return !find(Filter.builder()
                .conditions(filter == null ? Map.of() : filter.getConditions())
                .limit(1)
                .build()).isEmpty();
    }

    private void applyField(T entity, String field, Object value) {
        ColumnInfo column = metadata.getColumnByField(field);
        if (column == metadata.getPk() || column == metadata.getVersion()) {
            throw new QueryErrorException("'" + field + "' cannot be updated");
        }
        //연관 필드에 PK가 들어오면 해당 엔티티를 조회해서 넣는다
        if (column.isRelation() && value != null && !column.getRelationClass().isInstance(value)) {
            Object pk = value;
            value = Optional.ofNullable(entityManager.find(column.getRelationClass(), pk))
                    .orElseThrow(() -> new NotFoundException(
                            column.getRelationClass().getSimpleName() + "#" + pk + " not found"));
        }
        try {
            column.setValue(entity, value);
        } catch (IllegalArgumentException e) {
            throw new QueryErrorException("Invalid value type for '" + field + "': " + value, e);
        }
    }

    //연관 필드에 엔티티가 들어오면 해당 엔티티의 PK로 바꾼다
    private Object toQueryValue(ColumnInfo column, Object value) {
        if (!column.isRelation() || !column.getRelationClass().isInstance(value)) {
            return value;
        }
        return EntityMetadata.of(column.getRelationClass()).getPk().getValue(value);
    }

    //필드명 기반 Filter -> 컬럼명 기반 CriteriaQuery. 메타데이터에 없는 필드는 거부된다
    private CriteriaQuery<T> toCriteria(Filter filter) {
        CriteriaQuery<T> query = new CriteriaQuery<>(entityClass);
        if (filter == null) {
            return query;
        }
        filter.getConditions().forEach((field, value) -> {
            ColumnInfo info = metadata.getColumnByField(field);
            String column = info.columnName();
            if (value instanceof Collection<?> values) {
                query.whereIn(column, values.stream().map(v -> toQueryValue(info, v)).toList());
            } else {
                query.whereEqual(column, toQueryValue(info, value));
            }
        });

        for (Order order : filter.getSort().getOrders()) {
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
