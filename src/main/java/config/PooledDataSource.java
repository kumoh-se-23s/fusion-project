package config;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * 커넥션 풀. 처음 요청될 때 db.properties 값으로 초기화된다.
 */
public class PooledDataSource {
    private static volatile BasicDataSource basicDataSource;

    public static DataSource getDataSource() {
        if (basicDataSource == null) {
            synchronized (PooledDataSource.class) {
                if (basicDataSource == null) {
                    basicDataSource = create();
                }
            }
        }
        return basicDataSource;
    }

    private static BasicDataSource create() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(DBProperties.get("db.url"));
        dataSource.setUsername(DBProperties.get("db.username"));
        dataSource.setPassword(DBProperties.get("db.password", ""));
        dataSource.setInitialSize(DBProperties.getInt("db.pool.initial-size", 2));
        dataSource.setMaxTotal(DBProperties.getInt("db.pool.max-total", 10));
        return dataSource;
    }
}
