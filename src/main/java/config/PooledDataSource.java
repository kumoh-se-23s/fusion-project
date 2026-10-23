package config;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class PooledDataSource {
    private static BasicDataSource basicDataSource;

    public static DataSource getDataSource() {
        return basicDataSource;
    }
}
