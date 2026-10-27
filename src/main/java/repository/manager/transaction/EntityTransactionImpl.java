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

    @Override
    public void commit() {
        checkActive();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new TransactionException("Failed to commit Transaction", e);
        } finally {
            end();
        }
    }

    @Override
    public void rollback() {
        checkActive();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException("Failed to rollback Transaction", e);
        } finally {
            end();
        }
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
