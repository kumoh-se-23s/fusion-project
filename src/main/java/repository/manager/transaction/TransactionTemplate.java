package repository.manager.transaction;

import config.PooledDataSource;
import exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * 서비스 로직을 하나의 트랜잭션으로 감싸는 클래스
 * 정상 종료 시 commit, 예외 발생 시 rollback 후 예외를 그대로 던진다.
 * 이미 트랜잭션이 진행 중이면 새로 열지 않고 기존 트랜잭션에 참여한다. (readOnly는 바깥 트랜잭션 설정을 따른다)
 */
public class TransactionTemplate {

    public <R> R execute(Supplier<R> action) {
        return execute(false, action);
    }

    public <R> R execute(boolean readOnly, Supplier<R> action) {
        if (ConnectionHolder.isBound()) {
            return action.get();
        }

        Connection connection = openConnection();
        EntityTransaction transaction = new EntityTransactionImpl(connection, readOnly);
        ConnectionHolder.bind(connection);
        try {
            transaction.begin();
            R result = action.get();
            transaction.commit();
            return result;
        } catch (RuntimeException | Error e) {
            rollbackQuietly(transaction, e);
            throw e;
        } finally {
            ConnectionHolder.unbind();
            closeQuietly(connection);
        }
    }

    public void run(Runnable action) {
        run(false, action);
    }

    public void run(boolean readOnly, Runnable action) {
        execute(readOnly, () -> {
            action.run();
            return null;
        });
    }

    private Connection openConnection() {
        try {
            return PooledDataSource.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new TransactionException("Failed to get connection", e);
        }
    }

    private void rollbackQuietly(EntityTransaction transaction, Throwable cause) {
        if (!transaction.isActive()) {
            return;
        }
        try {
            transaction.rollback();
        } catch (RuntimeException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            //커넥션 반납 실패는 원래 결과에 영향을 주지 않음
        }
    }
}
