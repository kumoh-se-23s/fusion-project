package repository.manager;

public enum LockModeType {
    /** 락 없음 */
    NONE,

    /** 낙관적 락 (버전 비교) */
    OPTIMISTIC,

    /** 비관적 쓰기 락 (SELECT ... FOR UPDATE) */
    PESSIMISTIC_WRITE
}