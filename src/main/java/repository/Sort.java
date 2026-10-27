package repository;

import constant.OrderDirection;
import lombok.Getter;

import java.util.List;

@Getter
public class Sort {

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
