package repository.manager.transaction;

import exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityTransactionImpl implements EntityTransaction {
    private final Connection connection;
    private boolean active = false;

    public EntityTransactionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void begin() {
        if (active) {
            throw new TransactionException("Transaction already active");
        }
        try {
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

    public void checkActive() {
        if (!active) {
            throw new TransactionException("No active Transaction");
        }
    }
}
