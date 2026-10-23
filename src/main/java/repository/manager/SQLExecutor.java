package repository.manager;

import exception.QueryErrorException;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class SQLExecutor {
    public static Object execute(Connection connection, String sql) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new QueryErrorException("Query Fail\n" + sql, e);
        }
    }
}
