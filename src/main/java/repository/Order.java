package repository;

import constant.OrderDirection;

public record Order(String field, OrderDirection direction) {
        public static Order asc(String field) {
            return new Order(field, OrderDirection.ASC);
        }

        public static Order desc(String field) {
            return new Order(field, OrderDirection.DESC);
        }
    }
