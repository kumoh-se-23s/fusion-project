package repository.manager.query_dsl;

import annotation.Table;
import constant.OrderDirection;
import exception.LimitNumberOutException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 동적 Select Query를 조립해주는 빌더 클래스
 * 도메인에 @Table 어노테이션을 기반으로 테이블명을 매칭함
 * @param <T> 조회할 도메인 타입
 */

public class CriteriaQuery<T> {
    private final Class<T> resultClass;
    private final List<String> predicates = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private final List<String> orders = new ArrayList<>();

    private Integer limit;

    public CriteriaQuery(Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    //값이 null이면 IS NULL 조건. 조건을 빼면 전체 행이 조회된다
    public CriteriaQuery<T> whereEqual(String column, Object value) {
        if (value == null) {
            predicates.add(column + " IS NULL");
            return this;
        }
        predicates.add(column + " = ?");
        parameters.add(value);
        return this;
    }

    //값 목록 중 하나와 일치. 빈 목록이면 결과가 없다
    public CriteriaQuery<T> whereIn(String column, Collection<?> values) {
        if (values == null) {
            return this;
        }
        if (values.isEmpty()) {
            predicates.add("1 = 0");
            return this;
        }
        predicates.add(column + " IN (" + String.join(", ", Collections.nCopies(values.size(), "?")) + ")");
        parameters.addAll(values);
        return this;
    }

    public CriteriaQuery<T> whereLike(String column, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(column + " LIKE ?");
            parameters.add("%" + keyword.trim() + "%");
        }
        return this;
    }

    public CriteriaQuery<T> orderByDesc(String column) {
        return this.orderBy(column, OrderDirection.DESC.name());
    }

    public CriteriaQuery<T> orderByAsc(String column) {
        return this.orderBy(column, OrderDirection.ASC.name());
    }
    private CriteriaQuery<T> orderBy(String column, String orderType) {
        if (column != null && !column.isBlank()) {
            orders.add(column + " " + orderType);
        }
        return this;
    }

    public CriteriaQuery<T> limit(int limit) {
        if (limit < 0) {
            throw new LimitNumberOutException();
        }
        this.limit = limit;
        return this;
    }

    public String toSql() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ")
                .append(resolveTableName())
                .append(buildWhere())
                .append(buildOrderBy());

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        return sql.toString();
    }

    private String resolveTableName() {
        Table table = resultClass.getAnnotation(Table.class);
        return (table != null && !table.name().isEmpty())
                ? table.name()
                : resultClass.getSimpleName().toLowerCase();
    }

    private String buildWhere() {
        return predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
    }
    private String buildOrderBy() {
        return orders.isEmpty() ? "" : " ORDER BY " + String.join(", ", orders);
    }

    public Class<T> getResultClass() {
        return resultClass;
    }

    public List<Object> getParameters() {
        return parameters;
    }

}