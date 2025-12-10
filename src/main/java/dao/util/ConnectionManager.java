package dao.util;

import exception.DaoException;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String URL_KEY = "db.url";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static final String SQLITE = "org.sqlite.JDBC";
    private static final String CLOSE_METHOD = "close";
    private static BlockingQueue<Connection> pool;
    private static List<Connection> connections;

    static {
        loadDriver();
        getInitConnectionPool();
    }

    private static void getInitConnectionPool() {
        int poolSize = Integer.parseInt(PropertiesUtil.getProperties(POOL_SIZE_KEY));
        if (poolSize == 0) {
            poolSize = DEFAULT_POOL_SIZE;
        }
        pool = new ArrayBlockingQueue<>(poolSize);
        connections = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            Connection connection = ConnectionManager.open();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals(CLOSE_METHOD)
                            ? pool.add((Connection) proxy) : method.invoke(connection, args));
            pool.add(proxyConnection);
            connections.add(connection);
        }
    }

    public static Connection get() {
        try {
            getInitConnectionPool();
            return pool.take();
        } catch (InterruptedException e) {
            throw new DaoException(e);
        }
    }

    private ConnectionManager() {
    }

    private static Connection open() {
        try {
            return DriverManager.getConnection(PropertiesUtil.getProperties(URL_KEY));
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(SQLITE);
        } catch (ClassNotFoundException e) {
            throw new DaoException(e);
        }
    }

    public static void closePool() {
        for (Connection connection : connections) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
    }
}
