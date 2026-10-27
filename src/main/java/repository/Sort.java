package repository;

import constant.OrderDirection;
import lombok.Getter;

import java.util.List;

@Getter
public class Sort {
    public record Order(String field, OrderDirection direction) {
        public static Order asc(String field) {
            return new Order(field, OrderDirection.ASC);
        }

        public static Order desc(String field) {
            return new Order(field, OrderDirection.DESC);
        }
    }

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = orders;
    }

    public static Sort by(Order... orders) {
        return new Sort(List.of(orders));
    }

    public static Sort defaultSort() {
        return new Sort(List.of());
    }
}
