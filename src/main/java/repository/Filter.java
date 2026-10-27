package repository;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 조회 조건. conditions의 키는 도메인 필드명 (예: "funding"), 값은 일치 조건
 * 연관 필드의 값으로는 PK, 엔티티, Lazy 모두 사용 가능
 */
@Getter
@Builder
public class Filter {
    @Builder.Default
    private Map<String, Object> conditions = new HashMap<>();
    @Builder.Default
    private Sort sort = Sort.defaultSort();
    private Integer limit;

    public void and(String field, Object value) {
        conditions.put(field, value);
    }
}
