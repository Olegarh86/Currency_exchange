package exception;

public class AlreadyExistException extends RuntimeException {
    private static final String ALREADY_EXIST = "Already exist: ";

    public AlreadyExistException(String message) {
        super(ALREADY_EXIST + message);
    }
}
