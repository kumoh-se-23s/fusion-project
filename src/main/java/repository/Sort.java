package repository;

import constant.OrderDirection;

import java.util.ArrayList;
import java.util.List;

public record Sort(List<Order> orders) {


    public record Order(String field, OrderDirection direction) {}

    public static Sort asc(String field) {
        return new Sort(List.of(new Order(field, OrderDirection.ASC)));
    }

    public static Sort desc(String field) {
        return new Sort(List.of(new Order(field, OrderDirection.DESC)));
    }

    public Sort thenAsc(String field) {
        return and(new Order(field, OrderDirection.ASC));
    }

    public Sort thenDesc(String field) {
        return and(new Order(field, OrderDirection.DESC));
    }

    private Sort and(Order order) {
        List<Order> copy = new ArrayList<>(orders);
        copy.add(order);
        return new Sort(List.copyOf(copy));
    }
}
