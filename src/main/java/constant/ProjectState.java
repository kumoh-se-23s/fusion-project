package constant;

import exception.InvalidSearchTypeException;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum ProjectState {
    //프로젝트 생성만 됨
    JUST_USER_INPUT("JUST_USER_INPUT"),

    //승인 대기
    PENDING_APPROVAL("PENDING_APPROVAL"),

    //승인됨
    APPROVED("APPROVED"),

    //거절
    REJECTED("REJECTED"),

    //재거절
//    RE_REJECTED("RE_REJECTED"),

    //진행중
    IN_PROGRESS("IN_PROGRESS"),

    //완료
    COMPLETED("COMPLETED"),

    //실패
    FAILED("FAILED"),

    //취소
    CANCELED("CANCELED");

    private final String stringValue;

    private static final Map<String, ProjectState> BY_STRING_VALUE =
        Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(ProjectState::getStringValue, Function.identity()));


    ProjectState(String stringValue) {
        this.stringValue = stringValue;
    }

    public static ProjectState fromString(String stringValue) {
        ProjectState state = BY_STRING_VALUE.get(stringValue);
        if (state == null) {
            throw new InvalidSearchTypeException();
        }
        return state;
    }


    public static List<ProjectState> fromStringList(List<String> stringList) {
        return stringList.stream()
                .map(BY_STRING_VALUE::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
