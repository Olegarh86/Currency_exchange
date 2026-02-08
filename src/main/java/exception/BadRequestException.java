package exception;

public class BadRequestException extends RuntimeException {
    private static final String INVALID_PARAMETER = "Invalid parameter: ";

    public BadRequestException(String message) {
        super(INVALID_PARAMETER + message);
    }
}
