package exception;

public class NotFoundException extends RuntimeException {
    private static final String NOT_FOUND = "Not found: ";
    public NotFoundException(String message) {
        super(NOT_FOUND + message);
    }
}
