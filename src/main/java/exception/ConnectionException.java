package exception;

import java.sql.SQLException;

public class ConnectionException extends RuntimeException {
    public ConnectionException(SQLException e) {
        super("Connection Exception " + e);
    }
}
