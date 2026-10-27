package repository.manager;

import repository.manager.query_dsl.EntityMetadata;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 연관 엔티티(@JoinColumn)를 감싸는 지연 로딩 래퍼
 * getPk()는 쿼리 없이 FK 값을 반환하고, get()을 처음 호출할 때 엔티티를 조회한다.
 * 조회는 트랜잭션 안에서만 가능하며, 밖에서 호출하면 LazyInitializationException이 발생한다.
 *
 * @param <T> 연관 엔티티 타입
 */
public final class Lazy<T> {
    private final Class<T> type;
    private final Object pk;
    private Supplier<T> loader;
    private T value;
    private boolean loaded;

    private Lazy(Class<T> type, Object pk, T value, Supplier<T> loader, boolean loaded) {
        this.type = type;
        this.pk = pk;
        this.value = value;
        this.loader = loader;
        this.loaded = loaded;
    }

    //이미 가지고 있는 엔티티를 감쌈 (빌더로 새 엔티티를 만들 때 사용)
    @SuppressWarnings("unchecked")
    public static <T> Lazy<T> of(T entity) {
        Objects.requireNonNull(entity, "entity");
        return new Lazy<>((Class<T>) entity.getClass(), null, entity, null, true);
    }

    //pk만 알고 있는 참조. get() 호출 시 loader로 조회
    public static <T> Lazy<T> reference(Class<T> type, Object pk, Supplier<T> loader) {
        Objects.requireNonNull(pk, "pk");
        return new Lazy<>(type, pk, null, loader, false);
    }

    //연관 엔티티의 PK. 쿼리가 나가지 않는다.
    @SuppressWarnings("unchecked")
    public <K> K getPk() {
        if (loaded && value != null) {
            return (K) EntityMetadata.of(type).getPk().getValue(value);
        }
        return (K) pk;
    }

    public T get() {
        if (!loaded) {
            value = loader.get();
            loaded = true;
            loader = null;
        }
        return value;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Lazy[" + type.getSimpleName() + "#" + getPk() + (loaded ? "" : ", not loaded") + "]";
    }
}
