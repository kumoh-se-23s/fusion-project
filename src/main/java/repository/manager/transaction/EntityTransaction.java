package repository.manager.transaction;

/**
 * EntityManage에서 트랜잭션을 관리할 수 있도로 하는 EntityTransaction 인터페이스
 */

public interface EntityTransaction {
    void begin();

    void commit();

    void rollback();

    boolean isActive();

    boolean isReadOnly();
}
