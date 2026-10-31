package repository.manager.transaction;

import exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityTransactionImpl implements EntityTransaction {
    private final Connection connection;
    private final boolean readOnly;
    private boolean active = false;

    public EntityTransactionImpl(Connection connection) {
        this(connection, false);
    }

    public EntityTransactionImpl(Connection connection, boolean readOnly) {
        this.connection = connection;
        this.readOnly = readOnly;
    }

    @Override
    public void begin() {
        if (active) {
            throw new TransactionException("Transaction already active");
        }
        try {
            //readOnly는 트랜잭션 시작 전에 지정해야 한다
            connection.setReadOnly(readOnly);
            connection.setAutoCommit(false);
            active = true;
        } catch (SQLException e) {
            throw new TransactionException("Failed to begin Transaction", e);
        }
    }
    private void end() {
        active = false;
        try {
            connection.setAutoCommit(true);
            //풀에 반납되는 커넥션에 readOnly가 남지 않도록 복구
            connection.setReadOnly(false);
        } catch (SQLException e) {
            throw new TransactionException("Failed to restore auto commit", e);
        }
    }

    //commit/rollback 이후 end() 실행. end()가 실패해도 원래 예외가 덮이지 않도록 suppressed로 붙인다
    private void endAndThrow(TransactionException failure) {
        try {
            end();
        } catch (TransactionException endError) {
            if (failure == null) {
                throw endError;
            }
            failure.addSuppressed(endError);
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void commit() {
        checkActive();
        TransactionException failure = null;
        try {
            connection.commit();
        } catch (SQLException e) {
            //end()에서 autoCommit을 켜면 남은 작업이 커밋될 수 있으므로 먼저 롤백
            failure = new TransactionException("Failed to commit Transaction", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                failure.addSuppressed(rollbackError);
            }
        }
        endAndThrow(failure);
    }

    @Override
    public void rollback() {
        checkActive();
        TransactionException failure = null;
        try {
            connection.rollback();
        } catch (SQLException e) {
            failure = new TransactionException("Failed to rollback Transaction", e);
        }
        endAndThrow(failure);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void checkActive() {
        if (!active) {
            throw new TransactionException("No active Transaction");
        }
    }
}
