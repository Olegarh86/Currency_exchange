package dao.util;

import dao.ExchangeRateDao;
import exception.AlreadyExistException;
import exception.BadRequestException;

import java.math.BigDecimal;

public final class Validator {
    private static final String BAD_REQUEST_MESSAGE = "request error, check parameters";
    private static final String MESSAGE_ITSELF = "Exchange rate between the currency and itself is 1";
    private static final String EXIST_RATE = "Already exist rate";
    private static final String EXIST_REVERSE_RATE = "Already exist reverse rate";
    private static final String EXIST_CROSS_RATE = "There is already a cross exchange rate through the ";
    private static final String CODE_USD = "USD";

    public static void validateCurrency(String code, String name, String sign) {
        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE);
        }
    }

    public static void validateCode(String code) {
        if (code.length() != 3) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE);
        }
    }

    public static void validateRateOrAmount(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE);
        }
    }

    public static void validateInputParameters(String baseCode, String targetCode, BigDecimal value) {
        validateCode(baseCode);
        validateCode(targetCode);
        validateRateOrAmount(value);
    }

    public static void validateInputParameters(String baseCode, String targetCode, String rate) {
        validateCode(baseCode);
        validateCode(targetCode);
        BigDecimal newRate;
        try {
            newRate = new BigDecimal(rate);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE + e.getMessage());
        }
        validateRateOrAmount(newRate);
    }

    public static void validateExchangeRates(ExchangeRateDao instanceExchangeRate, String baseCode,
                                             String targetCode, BigDecimal rate) {
        validateInputParameters(baseCode, targetCode, rate);
        if (baseCode.equals(targetCode)) {
            throw new AlreadyExistException(MESSAGE_ITSELF);
        }

        if (instanceExchangeRate.rateIsExist(baseCode, targetCode)) {
            throw new AlreadyExistException(EXIST_RATE);
        }

        if (instanceExchangeRate.rateIsExist(targetCode, baseCode)) {
            throw new AlreadyExistException(EXIST_REVERSE_RATE);
        }

        if (instanceExchangeRate.rateIsExist(CODE_USD, baseCode) &&
            instanceExchangeRate.rateIsExist(CODE_USD, targetCode)) {
            throw new AlreadyExistException(EXIST_CROSS_RATE + CODE_USD);
        }

    }
}
