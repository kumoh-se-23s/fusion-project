package DTO;

import Annotation.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Serializer {

    //오류출력 Logger
    private static final Logger LOGGER = Logger.getLogger(Serializer.class.getName());

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

        try {
            checkBound(dtoType, domain.getClass());

            Constructor<T> constructor = dtoType.getDeclaredConstructor();
            constructor.setAccessible(true);
            T dto = constructor.newInstance();

            copyFields(dto, domain, true);
            return dto;

        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());

        } catch (NoSuchMethodException e) {
            LOGGER.warning(dtoType.getName() + " 클래스에 기본 생성자가 없습니다.");

        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "리플렉션 과정에서 에러가 발생하였습니다.", e);
        }

        return null;
    }

    //Domain -> DTOs
    public static <T extends Serializer> List<T> fromDomains(Collection<?> domains, Class<T> dtoType) {
        List<T> result = new ArrayList<>();

        for (Object domain : domains) {
            T dto = fromDomain(domain, dtoType);
            if (dto != null) result.add(dto);
        }

        return result;
    }

    // DTO -> Domain

    public <D> D toDomain(Class<D> domainType) {
        try {
            checkBound(this.getClass(), domainType);

            Constructor<D> constructor = domainType.getDeclaredConstructor();
            constructor.setAccessible(true);
            D domain = constructor.newInstance();

            copyFields(this, domain, false);
            return domain;

        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());

        } catch (NoSuchMethodException e) {
            LOGGER.warning(domainType.getName() + " 클래스에 기본 생성자가 없습니다.");

        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "리플렉션 과정에서 에러가 발생하였습니다.", e);
        }

        return null;
    }

    //DTOs -> Domain

    public static <D> List<D> toDomains(Collection<? extends Serializer> dtos, Class<D> domainType) {
        List<D> result = new ArrayList<>();

        for (Serializer dto : dtos) {
            if (dto == null) continue;

            D domain = dto.toDomain(domainType);
            if (domain != null) result.add(domain);
        }

        return result;
    }

    // DTO의 @BindDomain의 모델과 변환할 모델이 일치하는지 체크
    private static void checkBound(Class<?> dtoType, Class<?> domainType) {
        BindDomain bind = dtoType.getAnnotation(BindDomain.class);
        if (bind == null) {
            throw new IllegalArgumentException(
                dtoType.getName() + " 클래스에 @BindDomain이 선언되지 않았습니다."
            );
        }

        boolean bound = Arrays.stream(bind.value()).anyMatch(m -> m.isAssignableFrom(domainType));
        if (!bound) {
            throw new IllegalArgumentException(
                dtoType.getSimpleName() + "는 " + domainType.getSimpleName() + "를 변환할 수 없습니다.\n" +
                "허용된 타입: " + Arrays.stream(bind.value()).map(Class::getSimpleName).toList()
            );
        }
    }


    //필드 복사
    private static void copyFields(Object dto, Object domain, boolean domainToDto) throws IllegalAccessException {
        for (Class<?> c = dto.getClass(); c != null && c != Serializer.class; c = c.getSuperclass()) {
            for (Field dtoField : c.getDeclaredFields()) {
                if (Modifier.isStatic(dtoField.getModifiers())) continue;

                DomainField mapping = dtoField.getAnnotation(DomainField.class);
                String domainFieldName = (mapping != null) ? mapping.value() : dtoField.getName();

                Field domainField = findField(domain.getClass(), domainFieldName);
                if (domainField == null) {
                    throw new IllegalArgumentException(
                        domain.getClass().getSimpleName() + "에 '" + domainFieldName + "' 필드가 없습니다."
                    );
                }

                Field sourceField = domainToDto ? domainField : dtoField;
                Field targetField = domainToDto ? dtoField : domainField;

                if (!wrap(targetField.getType()).isAssignableFrom(wrap(sourceField.getType()))) {
                    throw new IllegalArgumentException(
                        "'" + dtoField.getName() + "' 필드의 타입이 일치하지 않습니다.\n" +
                        "dto: " + dtoField.getType().getSimpleName() +
                        ", domain: " + domainField.getType().getSimpleName()
                    );
                }

                dtoField.setAccessible(true);
                domainField.setAccessible(true);

                Object value = domainToDto ? domainField.get(domain) : dtoField.get(dto);

                if (value == null && (isNotNull(dtoField) || targetField.getType().isPrimitive())) {
                    throw new IllegalArgumentException(
                        "'" + dtoField.getName() + "' 필드는 null일 수 없습니다.\n" +
                        "domain: " + domain.getClass().getSimpleName()
                    );
                }

                if (domainToDto) dtoField.set(dto, value);
                else domainField.set(domain, value);
            }
        }
    }
    //NotNull 체크
    private static boolean isNotNull(Field field) {
        return field.getType().isPrimitive() || field.isAnnotationPresent(NotNull.class);
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