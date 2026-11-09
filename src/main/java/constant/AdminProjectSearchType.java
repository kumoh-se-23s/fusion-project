package constant;

import exception.InvalidSearchTypeException;
import lombok.Getter;
import repository.Order;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum AdminProjectSearchType {
    //최신순
    DESC_CREATE_AT("DESC_CREATE_AT", Order.desc("createAt")),

    //날짜순
    ASC_CREATE_AT("ASC_CREATE_AT", Order.desc("createAt")),

    //총 펀딩 금액 오름차순
    ASC_DONATE_AMOUNT("ASC_DONATE_AMOUNT", Order.desc("donateAmount")),

    //총 펀딩 금액 내림차순
    DESC_DONATE_AMOUNT("DESC_DONATE_AMOUNT", Order.desc("donateAmount")),

    //달성률 오름차순
    ASC_ACHIEVEMENT_RATE("ASC_ACHIEVEMENT_RATE"),

    //달성률 내림차순
    DESC_ACHIEVEMENT_RATE("DESC_ACHIEVEMENT_RATE"),

    //서포터 수 오름차순
    ASC_SUPPORTER_COUNT("ASC_SUPPORTER_COUNT"),

    //서포터 수 오름차순
    DESC_SUPPORTER_COUNT("DESC_SUPPORTER_COUNT"),;

    private final String stringValue;
    private final Optional<Order> order;

    private static final Map<String, AdminProjectSearchType> BY_STRING_VALUE =
        Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(AdminProjectSearchType::getStringValue, Function.identity()));


    AdminProjectSearchType(String stringValue, Order order) {
        this.stringValue = stringValue;
        this.order = Optional.ofNullable(order);
    }
    AdminProjectSearchType(String stringValue) {
        this.stringValue = stringValue;
        this.order = Optional.empty();
    }

    public static AdminProjectSearchType fromString(String stringValue) {
        AdminProjectSearchType adminProjectSearchType = BY_STRING_VALUE.get(stringValue);
        if (adminProjectSearchType == null) {
            throw new InvalidSearchTypeException(stringValue);
        }
        return adminProjectSearchType;
    }


}
