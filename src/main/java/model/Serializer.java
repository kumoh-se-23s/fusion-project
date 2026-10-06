package model;

import annotation.BindDataObject;
import annotation.BindDomain;
import exception.SerializeException;
import lombok.NonNull;
import model.domain.BaseDomain;
import model.dto.BaseData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Serializer {

    public static <DataObject extends BaseData, Domain extends BaseDomain> DataObject serialize(@NonNull Domain anyDomain, @NonNull Class<DataObject> targetClass) {
        // fast-fail
        Serializer.requireDataObjectBound(anyDomain);

        Class<?> domainClass = anyDomain.getClass();

        List<Class<?>> dtoClassList = List.of(domainClass.getDeclaredAnnotation(BindDataObject.class).values());

        if (!dtoClassList.contains(targetClass)) {
            throw new SerializeException("Attempted to serialize to an unregistered Data Object");
        }

        try {
            Constructor<DataObject> dataObjectConstructor = Serializer.findDefaultConstructor(targetClass);
            DataObject dataObject = Serializer.createInstance(dataObjectConstructor);
            Map<String, Field> fieldMap = Serializer.getFieldMap(targetClass);

            for (Field srcField : domainClass.getDeclaredFields()) {
                int srcMod = srcField.getModifiers();
                if (Modifier.isStatic(srcMod) || srcField.isSynthetic()) {
                    continue;
                }

                Field dstField = fieldMap.get(srcField.getName());
                if (dstField == null) {
                    continue; // 대상에 같은 이름의 필드가 없음
                }

                int dstMod = dstField.getModifiers();
                if (Modifier.isStatic(dstMod) || Modifier.isFinal(dstMod)) {
                    continue;
                }

                if (!dstField.getType().isAssignableFrom(srcField.getType())) {
                    continue; // 타입 불일치 (필요하면 예외 또는 변환 로직)
                }

                srcField.setAccessible(true);
                dstField.setAccessible(true);
                dstField.set(dataObject, srcField.get(anyDomain));
            }

            return dataObject;

        } catch (IllegalAccessException exception) {
            throw new SerializeException(
                    "Failed to access the field due to restricted visibility.",
                    exception
            );
        }
    }

    private static <DataObject extends BaseData> boolean isDomainBound(@NonNull DataObject anyData) {
        return anyData.getClass().isAnnotationPresent(BindDomain.class);
    }

    public static <DataObject extends BaseData> void requireDomainBound(@NonNull DataObject anyData) {
        if (!isDomainBound(anyData)) {
            throw new SerializeException("Attempted to serialize an object that is not bound to a domain.");
        }
    }

    private static <Domain extends BaseDomain> boolean isDataObjectBound(@NonNull Domain anyDomain) {
        return anyDomain.getClass().isAnnotationPresent(BindDataObject.class);
    }

    public static <Domain extends BaseDomain> void requireDataObjectBound(@NonNull Domain anyDomain) {
        if (!isDataObjectBound(anyDomain)) {
            throw new SerializeException("Attempted to serialize an object that is not bound to a data object.");
        }
    }

    private static <T> Constructor<T> findDefaultConstructor(@NonNull Class<T> targetClass) {
        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no default constructor for type" + targetClass.getName(), e);
        }
    }

    private static <T> T createInstance(@NonNull Constructor<T> constructor) {
        try {
            return constructor.newInstance();

        } catch (InvocationTargetException exception) {
            throw new SerializeException(
                    "An exception occurred while invoking the constructor of "
                            + constructor.getDeclaringClass().getName(),
                    exception
            );
        } catch (InstantiationException exception) {
            throw new SerializeException(
                    "Failed to instantiate the class "
                            + constructor.getDeclaringClass().getName()
                            + " because it is abstract, an interface, or lacks a default constructor.",
                    exception
            );
        } catch (IllegalAccessException exception) {
            throw new SerializeException(
                    "Failed to access the constructor of "
                            + constructor.getDeclaringClass().getName()
                            + " due to restricted visibility.",
                    exception
            );
        }
    }

    private static Map<String, Field> getFieldMap(@NonNull Class<?> anyClass) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Optional<Class<?>> maybeClass = Optional.of(anyClass);
             maybeClass.isPresent() && maybeClass.get() != Object.class;
             maybeClass = maybeClass.map(Class::getSuperclass)
        ) {
            for (Field field : maybeClass.get().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                field.setAccessible(true);
                fieldMap.putIfAbsent(field.getName(), field);
            }
        }

        return fieldMap;
    }

}

