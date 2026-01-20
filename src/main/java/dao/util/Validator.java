package dao.util;

import dao.ExchangeRateDao;
import exception.AlreadyExistException;
import exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class Validator {
    private static final String BAD_REQUEST_MESSAGE = "request error, check parameters: ";
    private static final String CODE_IS_EMPTY = "code is empty ";
    private static final String NAME_IS_EMPTY = "name is empty ";
    private static final String NOT_COMPLY_WITH_ISO_4217 = " code does not comply with ISO 4217 ";
    private static final String INVALID_NAME = "The currency name is too long. 100 characters are. Name: " +
                                               "allowed in the currency name";
    private static final String GREATER_THAN_0 = "Rate must be a number greater than 0 and have no more than 2 " +
                                                 "decimal places. Rate: ";
    private static final String MESSAGE_ITSELF = "Exchange rate between the currency and itself is 1";
    private static final String CODE_REGEX = "[A-Z][0-9]{3}";
    private static final String EXIST_RATE = "Already exist rate ";
    private static final String EXIST_REVERSE_RATE = "Already exist reverse rate ";
    private static final String EXIST_CROSS_RATE = "There is already a cross exchange rate through the ";
    private static final String CODE_USD = " USD ";
    private static final int NAME_MAX_LENGTH = 100;
    private static final int MAX_SCALE = 2;
    private static final Set<String> allCodes;

    private Validator() {
    }

    static {
        Set<Currency> allCurrencies = Currency.getAvailableCurrencies();
        allCodes = allCurrencies.stream().
                map(Currency::getCurrencyCode).
                collect(Collectors.toSet());
    }

    public static void validateCurrency(String code, String name) {
        if (code.isEmpty()) {
            throw new BadRequestException(CODE_IS_EMPTY);
        }
        if (name.isEmpty()) {
            throw new BadRequestException(NAME_IS_EMPTY);
        }
        validateCode(code);
        validateName(name);
    }

    private static void validateName(String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BadRequestException(INVALID_NAME + name);
        }
    }

    public static void validateCode(String code) {
        if (code.matches(CODE_REGEX) || !allCodes.contains(code)) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE + code + NOT_COMPLY_WITH_ISO_4217);
        }
    }

    private static void validateRateOrAmount(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(GREATER_THAN_0 + value);
        }
    }

    public static void validateInputParameters(String baseCode, String targetCode, BigDecimal value) {
        selfCheck(baseCode, targetCode);
        validateCode(baseCode);
        validateCode(targetCode);
        validateRateOrAmount(value);
    }

    public static void validateInputParameters(String baseCode, String targetCode, String rate) {
        selfCheck(baseCode, targetCode);
        validateCode(baseCode);
        validateCode(targetCode);
        BigDecimal newRate;
        try {
            newRate = new BigDecimal(rate);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BadRequestException(GREATER_THAN_0 + rate + e.getMessage());
        }

        if (newRate.scale() > MAX_SCALE) {
            throw new BadRequestException(GREATER_THAN_0 + rate);
        }
        validateRateOrAmount(newRate);
    }

    public static void validateExchangeRates(ExchangeRateDao instanceExchangeRate, String baseCode,
                                             String targetCode, BigDecimal rate) {
        validateInputParameters(baseCode, targetCode, rate);
        selfCheck(baseCode, targetCode);

        if (instanceExchangeRate.rateIsExist(baseCode, targetCode)) {
            throw new AlreadyExistException(EXIST_RATE + baseCode + targetCode);
        }

        if (instanceExchangeRate.rateIsExist(targetCode, baseCode)) {
            throw new AlreadyExistException(EXIST_REVERSE_RATE + targetCode + baseCode);
        }

        if (instanceExchangeRate.rateIsExist(CODE_USD, baseCode) &&
            instanceExchangeRate.rateIsExist(CODE_USD, targetCode)) {
            throw new AlreadyExistException(EXIST_CROSS_RATE + CODE_USD);
        }
    }

    private static void selfCheck(String baseCode, String targetCode) {
        if (baseCode.equals(targetCode)) {
            throw new AlreadyExistException(MESSAGE_ITSELF);
        }
    }
}
