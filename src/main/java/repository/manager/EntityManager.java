package repository.manager;

import repository.manager.query_dsl.CriteriaQuery;

import java.util.List;

/**
 * 도메인 단위의 기본 CRUD를 제공하는 EntityManager 인터페이스
 * 트랜잭션 안(TransactionTemplate)이면 그 커넥션을 쓰고, 밖이면 작업마다 커넥션을 열고 닫는다.
 */

public interface EntityManager {
    //INSERT 후 생성된 PK를 엔티티에 채운다
    void persist(Object entity);

    <T> T find(Class<T> entityClass, Object primaryKey);

    <T> T findWithLock(Class<T> entityClass, Object primaryKey, LockModeType lockMode);

    <T> List<T> findBy(CriteriaQuery<T> criteriaQuery);

    <T> List<T> findByWithLock(CriteriaQuery<T> criteriaQuery, LockModeType lockMode);

    //UPDATE. @Version이 있으면 버전이 다를 때 OptimisticLockException
    void merge(Object entity);

    //삭제된 행이 없으면 false
    boolean remove(Class<?> entityClass, Object primaryKey);
}
