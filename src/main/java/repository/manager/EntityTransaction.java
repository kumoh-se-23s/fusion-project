package repository.manager;

public interface EntityTransaction {
    void begin();

    void commit();

    void rollback();

    boolean isActive();
}
