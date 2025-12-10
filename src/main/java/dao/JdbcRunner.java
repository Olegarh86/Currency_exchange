package dao;

import dao.util.ConnectionManager;
import exception.DaoException;

import java.sql.*;

public class JdbcRunner {
    public static void main(String[] args) {
        try (Connection connection = ConnectionManager.get()) {
            getPrintln(connection);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            ConnectionManager.closePool();
        }

    }

    private static void getPrintln(Connection connection) throws SQLException {
        System.out.println(connection.getTransactionIsolation());
    }
}
