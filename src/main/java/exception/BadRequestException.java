package exception;

public class BadRequestException extends CurrencyExchangeException {
    public BadRequestException(String message) {
        super(message);
    }
}
