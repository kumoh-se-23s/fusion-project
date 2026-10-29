package model;

import annotation.BindDomain;
import annotation.DataField;
import exception.SerializeException;
import lombok.NonNull;
import model.domain.Domain;
import model.dto.DataObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain <-> DataObject 상호 변환기.
 *
 * <ul>
 *   <li>바인딩은 DataObject 쪽에만 {@link BindDomain} 으로 선언한다.</li>
 *   <li>DataObject 필드는 기본적으로 같은 이름의 도메인 필드와 매핑되며,
 *       {@link DataField#name()} 으로 다른 이름이나 경로("maker.pk")를 지정할 수 있다.</li>
 *   <li>필드 타입이 바인딩된 Domain/DataObject 이거나 그 컬렉션이면 재귀적으로 변환한다.</li>
 *   <li>DataObject 에 없는 도메인 필드(ex. version)는 변환 시 소거된다.</li>
 * </ul>
 */
public final class Serializer {
    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            int.class, Integer.class, long.class, Long.class,
            boolean.class, Boolean.class, double.class, Double.class,
            float.class, Float.class, short.class, Short.class,
            byte.class, Byte.class, char.class, Character.class
    );

    private static final Map<PlanKey, Plan> PLAN_CACHE = new ConcurrentHashMap<>();

    private enum Direction { TO_DATA, TO_DOMAIN }

    private record PlanKey(Class<?> source, Class<?> target) {}

    private record Plan(Constructor<?> constructor, List<Mapping> mappings) {}

    /**
     * @param dataField   DataObject 쪽 필드
     * @param domainPath  도메인 쪽 필드 경로 (길이 1 이면 직접 필드, 2 이상이면 연관 객체 내부 필드)
     * @param converter   source 값을 target 필드 타입으로 바꾸는 변환기
     */
    private record Mapping(Field dataField, List<Field> domainPath, Converter converter) {
        Field domainLeaf() {
            return domainPath.getLast();
        }
    }

    @FunctionalInterface
    private interface Converter {
        Object convert(Object value, Context context);
    }

    /** 한 번의 최상위 변환 동안 이미 변환한 객체를 기억해 순환 참조와 중복 생성을 막는다. */
    private static final class Context {
        private final Map<Object, Map<Class<?>, Object>> converted = new IdentityHashMap<>();

        Object find(Object source, Class<?> targetClass) {
            Map<Class<?>, Object> byType = converted.get(source);
            return byType == null ? null : byType.get(targetClass);
        }

        void remember(Object source, Class<?> targetClass, Object target) {
            converted.computeIfAbsent(source, ignored -> new HashMap<>()).put(targetClass, target);
        }
    }

    private Serializer() {}

    // ===================================================================== public API

    public static <Data extends DataObject> Data toData(Domain anyDomain, @NonNull Class<Data> dataClass) {
        return dataClass.cast(Serializer.convertObject(anyDomain, dataClass, Direction.TO_DATA, new Context()));
    }

    public static <Model extends Domain> Model toDomain(DataObject anyData, @NonNull Class<Model> domainClass) {
        return domainClass.cast(Serializer.convertObject(anyData, domainClass, Direction.TO_DOMAIN, new Context()));
    }

    public static <Data extends DataObject> List<Data> toDataList(Collection<? extends Domain> domains, @NonNull Class<Data> dataClass) {
        List<Data> result = new ArrayList<>();
        if (domains == null) return result;

        Context context = new Context();
        for (Domain domain : domains) {
            result.add(dataClass.cast(Serializer.convertObject(domain, dataClass, Direction.TO_DATA, context)));
        }
        return result;
    }

    public static <Model extends Domain> List<Model> toDomainList(Collection<? extends DataObject> datas, @NonNull Class<Model> domainClass) {
        List<Model> result = new ArrayList<>();
        if (datas == null) return result;

        Context context = new Context();
        for (DataObject data : datas) {
            result.add(domainClass.cast(Serializer.convertObject(data, domainClass, Direction.TO_DOMAIN, context)));
        }
        return result;
    }

    // ===================================================================== object conversion

    private static Object convertObject(Object source, Class<?> targetClass, Direction direction, Context context) {
        if (source == null) return null;

        Object cached = context.find(source, targetClass);
        if (cached != null) return cached;

        Class<?> dataClass = direction == Direction.TO_DATA ? targetClass : source.getClass();
        Class<?> domainClass = direction == Direction.TO_DATA ? source.getClass() : targetClass;
        Serializer.requireBound(dataClass, domainClass);

        Plan plan = Serializer.getPlan(source.getClass(), targetClass, direction);
        Object target = Serializer.createInstance(plan.constructor());
        // 필드를 채우기 전에 등록해야 순환 참조 시 같은 인스턴스를 재사용할 수 있다.
        context.remember(source, targetClass, target);

        for (Mapping mapping : plan.mappings()) {
            if (direction == Direction.TO_DATA) {
                Object value = Serializer.readPath(source, mapping.domainPath());
                Serializer.writeField(mapping.dataField(), target, mapping.converter().convert(value, context));
            } else {
                Object value = Serializer.readField(mapping.dataField(), source);
                Serializer.writePath(target, mapping.domainPath(), mapping.converter().convert(value, context));
            }
        }

        return target;
    }

    // ===================================================================== binding

    private static boolean isBound(Class<?> dataClass, Class<?> domainClass) {
        BindDomain binding = dataClass.getAnnotation(BindDomain.class);
        if (binding == null) return false;

        for (Class<?> bound : binding.values()) {
            if (bound.isAssignableFrom(domainClass)) return true;
        }
        return false;
    }

    private static void requireBound(Class<?> dataClass, Class<?> domainClass) {
        if (!dataClass.isAnnotationPresent(BindDomain.class)) {
            throw new SerializeException(dataClass.getName() + " is not bound to any domain. (missing @BindDomain)");
        }
        if (!Serializer.isBound(dataClass, domainClass)) {
            throw new SerializeException(
                    dataClass.getName() + " is not bound to " + domainClass.getName()
                            + ". bound: " + List.of(dataClass.getAnnotation(BindDomain.class).values())
            );
        }
    }

    // ===================================================================== plan

    private static Plan getPlan(Class<?> sourceClass, Class<?> targetClass, Direction direction) {
        PlanKey key = new PlanKey(sourceClass, targetClass);
        Plan plan = PLAN_CACHE.get(key);
        if (plan != null) return plan;

        // computeIfAbsent 안에서 재귀적으로 캐시를 건드리지 않도록 밖에서 만든 뒤 넣는다.
        plan = Serializer.buildPlan(sourceClass, targetClass, direction);
        Plan previous = PLAN_CACHE.putIfAbsent(key, plan);
        return previous != null ? previous : plan;
    }

    private static Plan buildPlan(Class<?> sourceClass, Class<?> targetClass, Direction direction) {
        Class<?> dataClass = direction == Direction.TO_DATA ? targetClass : sourceClass;
        Class<?> domainClass = direction == Direction.TO_DATA ? sourceClass : targetClass;

        List<Mapping> mappings = new ArrayList<>();
        for (Field dataField : Serializer.getFieldMap(dataClass).values()) {
            String path = Optional.ofNullable(dataField.getAnnotation(DataField.class))
                    .map(DataField::name)
                    .filter(name -> !name.isBlank())
                    .orElse(dataField.getName());

            List<Field> domainPath = Serializer.resolvePath(domainClass, path, dataField);
            Field domainLeaf = domainPath.getLast();

            Field targetField = direction == Direction.TO_DATA ? dataField : domainLeaf;
            if (Modifier.isFinal(targetField.getModifiers())) {
                throw new SerializeException(
                        "Cannot write final field '" + targetField.getName() + "' of " + targetField.getDeclaringClass().getName()
                );
            }

            Type sourceType = direction == Direction.TO_DATA ? domainLeaf.getGenericType() : dataField.getGenericType();
            Type targetType = direction == Direction.TO_DATA ? dataField.getGenericType() : domainLeaf.getGenericType();
            Converter converter = Serializer.resolveConverter(sourceType, targetType, direction, dataField);

            mappings.add(new Mapping(dataField, domainPath, converter));
        }

        // 직접 필드(user)를 먼저 쓰고 경로 필드(user.pk)를 나중에 써야
        // 경로 필드로 만든 연관 객체가 직접 필드의 null 로 덮이지 않는다.
        mappings.sort(Comparator.comparingInt(mapping -> mapping.domainPath().size()));

        return new Plan(Serializer.findDefaultConstructor(targetClass), List.copyOf(mappings));
    }

    private static List<Field> resolvePath(Class<?> domainClass, String path, Field dataField) {
        List<Field> fieldPath = new ArrayList<>();
        Class<?> current = domainClass;

        for (String name : path.split("\\.")) {
            Field field = Serializer.getFieldMap(current).get(name);
            if (field == null) {
                throw new SerializeException(
                        "Cannot map '" + dataField.getDeclaringClass().getSimpleName() + "." + dataField.getName()
                                + "': " + current.getName() + " has no field '" + name + "' (path: " + path + ")"
                );
            }
            fieldPath.add(field);
            current = field.getType();
        }

        return List.copyOf(fieldPath);
    }

    // ===================================================================== converter

    private static Converter resolveConverter(Type sourceType, Type targetType, Direction direction, Field ownerField) {
        Class<?> sourceRaw = Serializer.wrap(Serializer.rawType(sourceType));
        Class<?> targetRaw = Serializer.wrap(Serializer.rawType(targetType));

        // 컬렉션은 원소 타입이 다를 수 있으므로 단순 대입보다 먼저 검사한다. (List<Reward> -> List<RewardData>)
        if (Collection.class.isAssignableFrom(sourceRaw) && Collection.class.isAssignableFrom(targetRaw)) {
            Converter elementConverter = Serializer.resolveConverter(
                    Serializer.elementType(sourceType), Serializer.elementType(targetType), direction, ownerField
            );
            return (value, context) -> Serializer.convertCollection(value, targetRaw, elementConverter, context);
        }

        if (direction == Direction.TO_DATA
                && Domain.class.isAssignableFrom(sourceRaw)
                && DataObject.class.isAssignableFrom(targetRaw)
                && Serializer.isBound(targetRaw, sourceRaw)) {
            return (value, context) -> Serializer.convertObject(value, targetRaw, Direction.TO_DATA, context);
        }

        if (direction == Direction.TO_DOMAIN
                && DataObject.class.isAssignableFrom(sourceRaw)
                && Domain.class.isAssignableFrom(targetRaw)
                && Serializer.isBound(sourceRaw, targetRaw)) {
            return (value, context) -> Serializer.convertObject(value, targetRaw, Direction.TO_DOMAIN, context);
        }

        if (targetRaw.isAssignableFrom(sourceRaw)) {
            return (value, context) -> value;
        }

        throw new SerializeException(
                "Type mismatch on '" + ownerField.getDeclaringClass().getSimpleName() + "." + ownerField.getName()
                        + "': cannot convert " + sourceType.getTypeName() + " to " + targetType.getTypeName()
        );
    }

    @SuppressWarnings("unchecked")
    private static Object convertCollection(Object value, Class<?> collectionClass, Converter elementConverter, Context context) {
        if (value == null) return null;

        Collection<Object> result = (Collection<Object>) Serializer.createCollection(collectionClass);
        for (Object element : (Collection<?>) value) {
            result.add(elementConverter.convert(element, context));
        }
        return result;
    }

    private static Collection<?> createCollection(Class<?> collectionClass) {
        if (!collectionClass.isInterface() && !Modifier.isAbstract(collectionClass.getModifiers())) {
            return (Collection<?>) Serializer.createInstance(Serializer.findDefaultConstructor(collectionClass));
        }
        if (SortedSet.class.isAssignableFrom(collectionClass)) return new TreeSet<>();
        if (Set.class.isAssignableFrom(collectionClass)) return new LinkedHashSet<>();
        if (collectionClass.isAssignableFrom(ArrayList.class)) return new ArrayList<>();

        throw new SerializeException("Unsupported collection type " + collectionClass.getName());
    }

    // ===================================================================== field access

    private static Object readPath(Object root, List<Field> path) {
        Object current = root;
        for (Field field : path) {
            if (current == null) return null;
            current = Serializer.readField(field, current);
        }
        return current;
    }

    /** 경로 중간의 연관 객체가 비어 있으면 기본 생성자로 만들어 채운다. (makerPK -> maker = AppUser{pk}) */
    private static void writePath(Object root, List<Field> path, Object value) {
        Object current = root;

        for (Field field : path.subList(0, path.size() - 1)) {
            Object next = Serializer.readField(field, current);
            if (next == null) {
                if (value == null) return; // 값이 없으면 빈 연관 객체를 만들지 않는다.
                next = Serializer.createInstance(Serializer.findDefaultConstructor(field.getType()));
                Serializer.writeField(field, current, next);
            }
            current = next;
        }

        Serializer.writeField(path.getLast(), current, value);
    }

    private static Object readField(Field field, Object target) {
        try {
            return field.get(target);

        } catch (IllegalAccessException exception) {
            throw new SerializeException("Failed to read field '" + field.getName() + "' due to restricted visibility.", exception);
        }
    }

    private static void writeField(Field field, Object target, Object value) {
        // 원시형 필드에 null 을 넣을 수 없으므로 기본값을 유지한다.
        if (value == null && field.getType().isPrimitive()) return;

        try {
            field.set(target, value);

        } catch (IllegalAccessException exception) {
            throw new SerializeException("Failed to write field '" + field.getName() + "' due to restricted visibility.", exception);
        }
    }

    private static Map<String, Field> getFieldMap(@NonNull Class<?> anyClass) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
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

    // ===================================================================== instance

    private static <T> Constructor<T> findDefaultConstructor(@NonNull Class<T> targetClass) {
        if (targetClass.isInterface() || Modifier.isAbstract(targetClass.getModifiers())) {
            throw new SerializeException("Cannot instantiate abstract type " + targetClass.getName());
        }

        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;

        } catch (NoSuchMethodException exception) {
            throw new SerializeException("There is no default constructor for type " + targetClass.getName(), exception);
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
                            + " because it is abstract or an interface.",
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

    // ===================================================================== type util

    private static Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? WRAPPERS.getOrDefault(type, type) : type;
    }

    private static Class<?> rawType(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> Serializer.rawType(parameterized.getRawType());
            case WildcardType wildcard -> Serializer.rawType(wildcard.getUpperBounds()[0]);
            case TypeVariable<?> variable -> Serializer.rawType(variable.getBounds()[0]);
            case GenericArrayType array -> Serializer.rawType(array.getGenericComponentType()).arrayType();
            default -> Object.class;
        };
    }

    private static Type elementType(Type collectionType) {
        if (collectionType instanceof ParameterizedType parameterized) {
            return parameterized.getActualTypeArguments()[0];
        }
        return Object.class;
    }
}
