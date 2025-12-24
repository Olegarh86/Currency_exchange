package dao.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import exception.ConnectionException;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnector {
    private static final String URL_KEY = "db.path";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final String DEFAULT_POOL_SIZE_KEY = "db.default.pool.size";
    private static final String DRIVER_KEY = "db.driver";
    private static final HikariDataSource dataSource;

    private static final HikariConfig config = new HikariConfig();

    static {
        initDriver();
        config.setJdbcUrl(PropertiesUtil.getProperties(URL_KEY));
        config.setUsername("");
        config.setPassword("");
        config.setConnectionTimeout(10000);
        int poolSize = Integer.parseInt(PropertiesUtil.getProperties(POOL_SIZE_KEY));
        if (poolSize < 0) {
            poolSize = Integer.parseInt(PropertiesUtil.getProperties(DEFAULT_POOL_SIZE_KEY));
        }
        config.setMaximumPoolSize(poolSize);
        dataSource = new HikariDataSource(config);
    }

    private static void initDriver() {
        try {
            Class.forName(PropertiesUtil.getProperties(DRIVER_KEY));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
        return connection;
    }
}
