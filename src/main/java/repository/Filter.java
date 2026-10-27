package repository;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Filter {
    private Map<String, Object> conditions;
    @Builder.Default
    private Sort sort = Sort.defaultSort();
    private Integer limit;

    public void and(String field, Object value) {
        conditions.put(field, value);
    }
}
