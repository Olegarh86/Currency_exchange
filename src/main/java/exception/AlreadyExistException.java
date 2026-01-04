package exception;

public class AlreadyExistException extends CurrencyExchangeException {
    public AlreadyExistException(String message) {
        super("The requested resource already exists ");
    }
}
