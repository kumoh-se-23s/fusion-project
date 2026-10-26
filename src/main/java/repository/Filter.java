package repository;

import java.util.LinkedHashMap;
import java.util.Map;

/** 조건(필드명 = 값, AND) + 정렬 + limit. 키는 컬럼명이 아니라 엔티티 필드명이다. */
public record Filter(Map<String, Object> conditions, Sort sort, Integer limit) {

    public Filter {
        conditions = Map.copyOf(conditions);
        if (limit != null && limit < 1) {
            limit = null;
        }
    }

    public static Filter empty() {
        return new Filter(Map.of(), null, null);
    }

    public static Filter of(Map<String, Object> conditions) {
        return new Filter(conditions, null, null);
    }

    public static Filter eq(String field, Object value) {
        return of(Map.of(field, value));
    }

    public Filter and(String field, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(conditions);
        copy.put(field, value);
        return new Filter(copy, sort, limit);
    }

    public Filter sort(Sort sort) {
        return new Filter(conditions, sort, limit);
    }

    public Filter limit(int limit) {
        return new Filter(conditions, sort, limit);
    }
}