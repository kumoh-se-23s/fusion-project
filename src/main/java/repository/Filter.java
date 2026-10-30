package repository;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 조회 조건. conditions의 키는 도메인 필드명 (예: "funding"), 값은 일치 조건
 * 값이 Collection이면 IN 조건 (예: Map.of("reward", List.of(1L, 2L))). 빈 Collection이면 결과가 없다
 * 연관 필드의 값으로는 PK, 엔티티 모두 사용 가능
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
        //빌더로 Map.of 같은 불변 맵이 들어올 수 있으므로 복사해서 추가
        conditions = new HashMap<>(conditions);
        conditions.put(field, value);
    }
}
