package exception;

public class DaoException extends RuntimeException {
    public DaoException(Throwable throwable) {
        super(throwable);
    }
    public DaoException(String message) {
        super(message);
    }
}
