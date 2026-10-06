package model.dto;

import model.Convertible;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;

public abstract class BaseData {
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

                dataString.add("    \"" + field.getName() + "\" : "
                        + (value instanceof CharSequence ? "\"" + value + "\"" : value));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return dataString.toString();
    }

}
