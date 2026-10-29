package model.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class BaseData implements DataObject{
    public String toJsonString() {
        List<Field> fields = new ArrayList<>();

        for (Optional<Class<?>> maybeClass = Optional.of(this.getClass());
             maybeClass.isPresent() && maybeClass.get() != Object.class;
             maybeClass = maybeClass.map(Class::getSuperclass)
        ) {
            fields.addAll(List.of(maybeClass.get().getDeclaredFields()));
        }

        StringJoiner dataString = new StringJoiner(",\n", "{\n", "\n}");
        dataString.setEmptyValue("{}");

        try {
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(this);

                dataString.add("    " + quote(field.getName()) + " : " + toJsonValue(value));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return dataString.toString();
    }

    //필드 값을 JSON 값으로 변환. 숫자/불리언은 그대로, 날짜/enum/문자열은 따옴표, 중첩 DTO와 컬렉션은 재귀 변환
    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof BaseData data) {
            return data.toJsonString();
        }
        if (value instanceof byte[] bytes) {
            return quote(Base64.getEncoder().encodeToString(bytes));
        }
        if (value instanceof Collection<?> collection) {
            StringJoiner array = new StringJoiner(", ", "[", "]");
            for (Object element : collection) {
                array.add(toJsonValue(element));
            }
            return array.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringJoiner object = new StringJoiner(", ", "{", "}");
            map.forEach((k, v) -> object.add(quote(String.valueOf(k)) + " : " + toJsonValue(v)));
            return object.toString();
        }
        if (value instanceof Enum<?> e) {
            return quote(e.name());
        }
        //문자열, 날짜(LocalDateTime 등), 그 외 타입은 toString() 결과를 문자열로
        return quote(value.toString());
    }

    private static String quote(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
