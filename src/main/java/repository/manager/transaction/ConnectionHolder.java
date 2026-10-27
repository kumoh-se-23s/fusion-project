package repository.manager.transaction;

import java.sql.Connection;

/**
 * 현재 스레드에서 진행 중인 트랜잭션의 커넥션을 보관하는 클래스
 * 같은 트랜잭션 안의 리포지토리들이 하나의 커넥션을 공유하도록 한다.
 * 트랜잭션마다 새 Scope를 만들어서, 같은 스레드에서 열린 다른 트랜잭션과 구분할 수 있게 한다.
 */
public final class ConnectionHolder {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    /**
     * 트랜잭션 하나를 나타내는 객체. 풀에서 같은 커넥션을 다시 받아도 트랜잭션이 다르면 다른 Scope이다.
     * 비교는 참조(==)로 한다.
     */
    public static final class Scope {
        private final Connection connection;

        private Scope(Connection connection) {
            this.connection = connection;
        }
    }

    private ConnectionHolder() {
    }

    //트랜잭션 밖이면 null
    public static Connection get() {
        Scope scope = CURRENT.get();
        return scope == null ? null : scope.connection;
    }

    public static boolean isBound() {
        return CURRENT.get() != null;
    }

    //현재 트랜잭션. 트랜잭션 밖이면 null
    public static Scope currentScope() {
        return CURRENT.get();
    }

    static void bind(Connection connection) {
        CURRENT.set(new Scope(connection));
    }

    static void unbind() {
        CURRENT.remove();
    }
}
