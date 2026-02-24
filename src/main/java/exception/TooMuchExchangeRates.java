package exception;

public class TooMuchExchangeRates extends RuntimeException {
  private static final String TOO_MUCH_EXCHANGE_RATES = "Too Much Exchange Rates, i can't choose the right exchange " +
          "rate";
    public TooMuchExchangeRates(String message) {

      super(TOO_MUCH_EXCHANGE_RATES + message);
    }
}
