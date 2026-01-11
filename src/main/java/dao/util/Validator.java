package dao.util;

import dao.ExchangeRateDao;
import exception.AlreadyExistException;
import exception.BadRequestException;

import java.math.BigDecimal;

public final class Validator {
    private static final String badRequestMessage = "request error, check parameters";
    private static final String CODE_USD = "USD";

    public static void validateCurrency(String code, String name, String sign) {
        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            throw new BadRequestException(badRequestMessage);
        }
    }

    public static void validateCode(String code) {
        if (code.length() != 3) {
            throw new BadRequestException(badRequestMessage);
        }
    }

    public static void validateRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(badRequestMessage);
        }
    }

    public static void validateInputParameters(String baseCode, String targetCode, BigDecimal rate) {
        validateCode(baseCode);
        validateCode(targetCode);
        validateRate(rate);
    }

    public static void validateInputParameters(String baseCode, String targetCode, String rate) {
        validateCode(baseCode);
        validateCode(targetCode);
        BigDecimal newRate;
        try {
            newRate = new BigDecimal(rate);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BadRequestException(badRequestMessage + e.getMessage());
        }
        validateRate(newRate);
    }

    public static void validateExchangeRates(ExchangeRateDao instanceExchangeRate, String baseCode,
                                             String targetCode, BigDecimal rate) {
        validateInputParameters(baseCode, targetCode, rate);
        if (baseCode.equals(targetCode)) {
            throw new AlreadyExistException("Can't add that. The exchange rate between the currency and itself is 1");
        }

        if (instanceExchangeRate.rateIsExist(baseCode, targetCode)) {
            throw new AlreadyExistException("Already exist rate");
        }

        if (instanceExchangeRate.rateIsExist(targetCode, baseCode)) {
            throw new AlreadyExistException("Already exist reverse rate");
        }

        if (instanceExchangeRate.rateIsExist(CODE_USD, baseCode) &&
            instanceExchangeRate.rateIsExist(CODE_USD, targetCode)) {
            throw new AlreadyExistException("There is already a cross exchange rate through the " + CODE_USD);
        }

    }
}
