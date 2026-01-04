package exception;

public class NotFoundException extends CurrencyExchangeException {
    public NotFoundException(String message) {
        super(message);
    }
}
