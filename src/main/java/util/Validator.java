package util;

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
    private static final String GREATER_THAN_0 = "Rate must be a number greater than 0 and have no more than 6 " +
                                                 "decimal places. Rate: ";
    private static final String MESSAGE_ITSELF = "Exchange rate between the currency and itself is 1";
    private static final String CODE_REGEX = "[A-Z][0-9]{3}";
    private static final int NAME_MAX_LENGTH = 100;
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
        validateCode(code);
        validateName(name);
    }

    private static void validateName(String name) {
        if (name.isEmpty()) {
            throw new BadRequestException(NAME_IS_EMPTY);
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new BadRequestException(INVALID_NAME + name);
        }
    }

    public static void validateCode(String code) {
        if (code.isEmpty()) {
            throw new BadRequestException(CODE_IS_EMPTY);
        }

        if (code.matches(CODE_REGEX) || !allCodes.contains(code)) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE + code + NOT_COMPLY_WITH_ISO_4217);
        }
    }

    public static void validateInputParameters(String baseCode, String targetCode, String rate) {
        selfCheck(baseCode, targetCode);
        validateCode(baseCode);
        validateCode(targetCode);
        validateRateString(rate);
    }

    private static void validateRateString(String rate) {
        BigDecimal newRate;
        try {
            newRate = new BigDecimal(rate);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BadRequestException(BAD_REQUEST_MESSAGE + rate + e.getMessage());
        }

        if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(GREATER_THAN_0 + rate);
        }

        if (newRate.scale() > 6) {
            throw new BadRequestException(GREATER_THAN_0 + rate);
        }
    }

    public static void selfCheck(String baseCode, String targetCode) {
        if (baseCode.equals(targetCode)) {
            throw new AlreadyExistException(MESSAGE_ITSELF);
        }
    }
}
