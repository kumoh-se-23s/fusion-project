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
        dataSource.setUrl(PropertieData.get("db.url"));
        dataSource.setUsername(PropertieData.get("db.username"));
        dataSource.setPassword(PropertieData.get("db.password", ""));
        dataSource.setInitialSize(PropertieData.getInt("db.pool.initial-size", 2));
        dataSource.setMaxTotal(PropertieData.getInt("db.pool.max-total", 10));
        return dataSource;
    }
}
