package constant;

import exception.InvalidSearchTypeException;
import lombok.Getter;
import repository.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum ProjectSearchType {
    //최신순
    DESC_CREATE_AT(
            "DESC_CREATE_AT",
            Order.desc("createdAt")),

    //날짜순
    ASC_CREATE_AT(
            "ASC_CREATE_AT",
            Order.asc("createdAt")),

    //마감임박순
    ASC_END_DATE(
            "ASC_END_DATE",
            Order.asc("endDate"),
            List.of(ProjectState.IN_PROGRESS)),


    //모집금액순
    DESC_AMOUNT(
            "DESC_AMOUNT",
            Order.desc("donateAmount")),

    //좋아요 순
    DESC_LIKE_COUNT(
            "DESC_LIKE_COUNT",
            Order.desc("likeCount"));

    private final String stringValue;
    private final Order order;
    private final List<ProjectState> projectStateList;


    private static final Map<String, ProjectSearchType> BY_STRING_VALUE =
        Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(ProjectSearchType::getStringValue, Function.identity()));

    ProjectSearchType(String stringValue, Order order, List<ProjectState> projectStateList) {
        this.stringValue = stringValue;
        this.order = order;
        this.projectStateList = projectStateList;
    }
    ProjectSearchType(String stringValue, Order order) {
        this.stringValue = stringValue;
        this.order = order;
        this.projectStateList = List.of();
    }

    public static ProjectSearchType fromString(String stringValue) {
        ProjectSearchType searchType = BY_STRING_VALUE.get(stringValue);
        if (searchType == null) {
            throw new InvalidSearchTypeException();
        }
        return searchType;
    }
}
