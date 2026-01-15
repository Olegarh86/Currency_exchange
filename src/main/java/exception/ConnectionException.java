package exception;

public class ConnectionException extends RuntimeException {
    public ConnectionException(String e) {
        super("Connection Exception " + e);
    }
}
