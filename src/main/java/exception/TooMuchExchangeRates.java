package exception;

public class TooMuchExchangeRates extends RuntimeException {
  public TooMuchExchangeRates(String message) {
    super(message);
  }
}
