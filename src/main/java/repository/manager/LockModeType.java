package repository.manager;

/**
 *
 * 동시성 문제를 해결하기 위한 락 처리 Enum
 *
 */
public enum LockModeType {
    /** 락 없음 */
    NONE,

    /** 낙관적 락 (버전 비교) */
    OPTIMISTIC,

    /** 비관적 쓰기 락 (SELECT ... FOR UPDATE) */
    PESSIMISTIC_WRITE
}