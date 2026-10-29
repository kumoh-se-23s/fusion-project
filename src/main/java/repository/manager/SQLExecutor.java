package repository.manager;

import exception.AlreadyExistException;
import exception.QueryErrorException;
import repository.manager.mapper.JdbcValueConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 모든 쿼리가 지나가는 실행 계층
 * 파라미터 변환, 리소스 정리, SQLException 변환, SQL 로깅을 한곳에서 처리한다.
 */
public final class SQLExecutor {
    //MySQL 에러 코드 1062 (ER_DUP_ENTRY): UNIQUE 제약 위반
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private SQLExecutor() {
    }

    public static <T> List<T> query(Connection connection, String sql, List<?> params, RowMapper<T> mapper) {
        long start = System.nanoTime();
        List<Object> values = toJdbc(params);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, values);
            List<T> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
            SqlLogger.query(sql, values, result.size() + " rows", start);
            return result;
        } catch (SQLException e) {
            throw new QueryErrorException("Query Fail\n" + sql, e);
        }
    }

    //UPDATE, DELETE. 영향받은 행 수 반환
    public static int update(Connection connection, String sql, List<?> params) {
        long start = System.nanoTime();
        List<Object> values = toJdbc(params);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, values);
            int rows = ps.executeUpdate();
            SqlLogger.query(sql, values, rows + " affected", start);
            return rows;
        } catch (SQLException e) {
            throw translate("Query Fail\n" + sql, e);
        }
    }

    //INSERT. DB가 생성한 키를 keyType으로 반환 (keyType이 null이면 키를 받지 않음)
    public static <K> K insert(Connection connection, String sql, List<?> params, Class<K> keyType) {
        long start = System.nanoTime();
        List<Object> values = toJdbc(params);
        int keyOption = keyType == null ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS;
        try (PreparedStatement ps = connection.prepareStatement(sql, keyOption)) {
            bind(ps, values);
            int rows = ps.executeUpdate();
            K key = null;
            if (keyType != null) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        key = keys.getObject(1, keyType);
                    }
                }
            }
            SqlLogger.query(sql, values, rows + " inserted" + (key != null ? ", key=" + key : ""), start);
            return key;
        } catch (SQLException e) {
            throw translate("Insert Fail\n" + sql, e);
        }
    }

    //UNIQUE 제약 위반(중복 키)은 비즈니스 예외(409)로, 나머지는 쿼리 오류로 변환
    private static RuntimeException translate(String message, SQLException e) {
        if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
            //DB 메시지에는 중복된 실제 값이 들어있으므로 메시지에 노출하지 않고 cause로만 보관
            return new AlreadyExistException("Duplicate value", e);
        }
        return new QueryErrorException(message, e);
    }

    private static List<Object> toJdbc(List<?> params) {
        List<Object> values = new ArrayList<>(params.size());
        for (Object param : params) {
            values.add(JdbcValueConverter.toJdbc(param));
        }
        return values;
    }

    private static void bind(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
    }
}
