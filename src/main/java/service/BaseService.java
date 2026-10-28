package service;

import repository.manager.transaction.TransactionTemplate;

/**
 * 서비스 공통 부모 클래스
 * 서비스 메서드는 tx.execute()로 감싸서 하나의 트랜잭션으로 실행한다. (조회만 하면 tx.execute(true, ...))
 */
public abstract class BaseService {
    protected final TransactionTemplate tx = new TransactionTemplate();
}
