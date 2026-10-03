package DTO;

import Annotation.BindModel;
import Annotation.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.io.*;

public class ModelSerializer<T> {
    private final Class<T> targetObjectType;

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            int.class, Integer.class, long.class, Long.class,
            boolean.class, Boolean.class, double.class, Double.class,
            float.class, Float.class, short.class, Short.class,
            byte.class, Byte.class, char.class, Character.class
    ); //primitive랑 object 같게 취급

    public ModelSerializer(Class<T> targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    private boolean isNotNull(Field field) { //null 체크
        return field.getType().isPrimitive() || field.isAnnotationPresent(NotNull.class);
    }

    private void validate(Object dto) {
        BindModel bind =
                dto.getClass().getAnnotation(BindModel.class);

        if (bind == null) {
            throw new IllegalArgumentException(
                    dto.getClass().getName()
                            + " 클래스에 @BindModel이 선언되지 않았습니다."
            );
        }

        boolean bound = Arrays.stream(bind.value())
                .anyMatch(model -> model.equals(targetObjectType));

        if (!bound) {
            throw new IllegalArgumentException(
                    dto.getClass().getSimpleName()
                            + "는 "
                            + targetObjectType.getSimpleName()
                            + "으로 변환할 수 없습니다.\n"
                            + "허용된 타입: "
                            + Arrays.stream(bind.value())
                            .map(Class::getSimpleName)
                            .toList()
            );
        }
    }

    private Field findField(Class<?> type, String name) { //필드 비교
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? WRAPPERS.get(type) : type;
    }

    private void copyField(
            Object dto,
            T model,
            Field dtoField
    ) throws IllegalAccessException {

        Field modelField =
                findField(targetObjectType, dtoField.getName());

        // DTO의 필드는 Domain에 반드시 존재해야 함
        if (modelField == null) {
            throw new IllegalArgumentException(
                    targetObjectType.getSimpleName()
                            + "에 '"
                            + dtoField.getName()
                            + "' 필드가 없습니다."
            );
        }

        // 타입 검사
        if (!wrap(dtoField.getType())
                .equals(wrap(modelField.getType()))) {

            throw new IllegalArgumentException(
                    "'"
                            + dtoField.getName()
                            + "' 필드의 타입이 일치하지 않습니다.\n"
                            + "dto: "
                            + dtoField.getType().getSimpleName()
                            + ", model: "
                            + modelField.getType().getSimpleName()
            );
        }

        dtoField.setAccessible(true);
        modelField.setAccessible(true);

        // ⭐ DTO에서 값을 가져와야 함
        Object value = dtoField.get(dto);

        if (value == null && isNotNull(dtoField)) {
            throw new IllegalArgumentException(
                    "'"
                            + dtoField.getName()
                            + "' 필드는 null일 수 없습니다."
            );
        }

        // ⭐ Model에 값을 넣어야 함
        modelField.set(model, value);
    }

    public T toEntity(Object entity) {
    if (entity == null) return null;

    try {
        this.validate(entity);

        Constructor<T> constructor =
            this.targetObjectType.getDeclaredConstructor();

        constructor.setAccessible(true);
        T dto = constructor.newInstance();

        for (Field dtoField : this.targetObjectType.getDeclaredFields()) {
            if (Modifier.isStatic(dtoField.getModifiers())) continue;

            this.copyField(entity, dto, dtoField);
        }

        return dto;

    } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            this.targetObjectType.getName()
                + " 클래스에 기본 생성자가 없습니다.", e);

    } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(
            "리플렉션 과정에서 오류가 발생했습니다.",
            e
        );
    }
}


    public Optional<T> toEntityOptional(Object entity) {
        return Optional.ofNullable(this.toEntity(entity));
    }

    public List<T> toEntityMany(Collection<?> entities) {
        List<T> result = new ArrayList<>();

        for (Object entity : entities) {
            T dto = this.toEntity(entity);
            // null 요소와 실패한 항목은 제외 (실패 안내는 serialize 내부에서 이미 출력됨)
            if (dto != null) result.add(dto);
        }
        return result;
    }
}
