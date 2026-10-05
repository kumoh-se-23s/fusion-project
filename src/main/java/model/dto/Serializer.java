package model.dto;

import Annotation.*;

import java.lang.reflect.*;
import java.util.*;


import Exception.SerializeException;

public abstract class Serializer {

    //원시형 -> 래퍼클래스     아마 필요 없을듯?
    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
        int.class, Integer.class, long.class, Long.class,
        boolean.class, Boolean.class, double.class, Double.class,
        float.class, Float.class, short.class, Short.class,
        byte.class, Byte.class, char.class, Character.class
    );


    //Domain -> DTO
    public static <T extends Serializer> T fromDomain(Object domain, Class<T> dtoType) {
        if (domain == null) return null;

        checkBound(dtoType, domain.getClass());

        T dto = newInstance(dtoType);
        copyFields(dto, domain, true);
        return dto;

    }

    //Domain -> DTOs
    public static <T extends Serializer> List<T> fromDomains(Collection<?> domains, Class<T> dtoType) {
        List<T> result = new ArrayList<>();
        if (domains == null) return result;

        for (Object domain : domains) {
            T dto = fromDomain(domain, dtoType);
            if (dto != null) result.add(dto);
        }

        return result;
    }

    // DTO -> Domain

    public <D> D toDomain(Class<D> domainType) {
        checkBound(this.getClass(), domainType);

        D domain = newInstance(domainType);
        copyFields(this, domain, false);
        return domain;

    }

    //DTOs -> Domain

    public static <D> List<D> toDomains(Collection<? extends Serializer> dtos, Class<D> domainType) {
        List<D> result = new ArrayList<>();
        if (dtos == null) return result;

        for (Serializer dto : dtos) {
            if (dto != null) result.add(dto.toDomain(domainType));
        }
        return result;
    }

    // DTO의 @BindDomain의 모델과 변환할 모델이 일치하는지 체크
    private static void checkBound(Class<?> dtoType, Class<?> domainType) {
        BindDomain bind = dtoType.getAnnotation(BindDomain.class);
        if (bind == null) {
            throw new SerializeException(dtoType.getName() + " 클래스에 @BindDomain이 선언되지 않았습니다.");
        }

        boolean bound = Arrays.stream(bind.value()).anyMatch(m -> m.isAssignableFrom(domainType));
        if (!bound) {
            throw new SerializeException(
                dtoType.getSimpleName() + "는 " + domainType.getSimpleName() + "를 변환할 수 없습니다.\n" +
                "허용된 타입: " + Arrays.stream(bind.value()).map(Class::getSimpleName).toList()
            );
        }
    }


    //필드 복사
    private static void copyFields(Object dto, Object domain, boolean domainToDto) {
        for (Class<?> c = dto.getClass(); c != null && c != Serializer.class; c = c.getSuperclass()) {
            for (Field dtoField : c.getDeclaredFields()) {
                if (Modifier.isStatic(dtoField.getModifiers())) continue;

                DomainField mapping = dtoField.getAnnotation(DomainField.class);  //어노테이션 존재 시 어노테이션 값으로 조회
                String path = (mapping != null) ? mapping.value() : dtoField.getName();

                Field domainField = resolveField(domain.getClass(), path);

                Field sourceField = domainToDto ? domainField : dtoField;
                Field targetField = domainToDto ? dtoField : domainField;

                if (!wrap(targetField.getType()).isAssignableFrom(wrap(sourceField.getType()))) {
                    throw new SerializeException(
                        "'" + dtoField.getName() + "' 필드의 타입이 일치하지 않습니다.\n" +
                        "dto: " + dtoField.getType().getSimpleName() +
                        ", domain: " + domainField.getType().getSimpleName()
                    );
                }

                if (!domainToDto && path.contains(".")) continue;

                Object value = domainToDto ? readPath(domain, path) : get(dtoField, dto);

                if (value == null && targetField.getType().isPrimitive()) {
                    throw new SerializeException(
                        "'" + dtoField.getName() + "' 필드는 " + targetField.getType().getSimpleName() +
                        " 타입이라 null을 넣을 수 없습니다.\n" +
                        "domain: " + domain.getClass().getSimpleName()
                    );
                }

                if (domainToDto) set(dtoField, dto, value);
                else set(domainField, domain, value);
            }
        }
    }
    //다른 객체를 필드로 받는 경우     @DomainField에 ("Object.FieldName")와 같이 작성하여 사용
    private static Field resolveField(Class<?> domainType, String path) {
        Field field = null;
        Class<?> current = domainType;

        for (String name : path.split("\\.")) {
            field = findField(current, name);
            if (field == null) {
                throw new SerializeException(
                    current.getSimpleName() + "에 '" + name + "' 필드가 없습니다. (경로: " + path + ")"
                );
            }
            current = field.getType();
        }

        return field;
    }
    private static Object readPath(Object domain, String path) {
        Object current = domain;

        for (String name : path.split("\\.")) {
            if (current == null) return null;
            Field field = findField(current.getClass(), name);

            if (field == null) {
                throw new SerializeException(
                    current.getClass().getSimpleName() +
                    "에 '" + name + "' 필드가 없습니다. (경로: " + path + ")"
                );
            }
            current = get(field, current);

        }

        return current;
    }
    private static <C> C newInstance(Class<C> type) {
        try {
            Constructor<C> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();

        } catch (NoSuchMethodException e) {
            throw new SerializeException(type.getName() + " 클래스에 기본 생성자가 없습니다.", e);

        } catch (ReflectiveOperationException e) {
            throw new SerializeException(type.getName() + " 인스턴스 생성에 실패했습니다.", e);
        }
    }

    private static Object get(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new SerializeException("'" + field.getName() + "' 필드를 읽지 못했습니다.", e);
        }
    }

    private static void set(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new SerializeException("'" + field.getName() + "' 필드에 값을 넣지 못했습니다.", e);
        }
    }

    //필드 찾기
    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    // 원시형 -> 래퍼로 변환    아마 필요없을듯?
    private static Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? WRAPPERS.get(type) : type;
    }
}