package util;

import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.CurrencyRequestDto;
import exception.AlreadyExistException;
import exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class Validator {
    private static final String BAD_REQUEST_MESSAGE = ": erroneously entered value. Enter a number greater " +
                                                      "than 0 and have no more than 6 decimal places.";
    private static final String CODE_IS_EMPTY = "code is empty ";
    private static final String NAME_IS_EMPTY = "name is empty ";
    private static final String NOT_COMPLY_WITH_ISO_4217 = " code does not comply with ISO 4217";
    private static final String INVALID_NAME = "The currency name is too long. 100 characters are. Name: " +
                                               "allowed in the currency name";
    private static final String MESSAGE_ITSELF = "Exchange rate between the currency and itself is 1";
    private static final int NAME_MAX_LENGTH = 100;
    private static final int MAX_SCALE = 6;
    private static final String EXIST_RATE = "Already exist rate ";
    private static final String EXIST_REVERSE_RATE = "Already exist reverse rate ";
    private static final String EXIST_CROSS_RATE = "There is already a cross exchange rate through the";
    private static final String SEPARATOR = " - ";
    private static final CurrencyDto USD_DTO = new CurrencyRequestDto("USD");
    private static final BigDecimal LOWER_BOUND_VALUE = BigDecimal.ZERO;
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

        if (!allCodes.contains(code)) {
            throw new BadRequestException(code + NOT_COMPLY_WITH_ISO_4217);
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
            throw new BadRequestException(rate + BAD_REQUEST_MESSAGE);
        }

        if (newRate.compareTo(LOWER_BOUND_VALUE) <= 0 || newRate.scale() > MAX_SCALE) {
            throw new BadRequestException(rate + BAD_REQUEST_MESSAGE);
        }
    }

    private static void selfCheck(String baseCode, String targetCode) {
        if (baseCode.equals(targetCode)) {
            throw new AlreadyExistException(MESSAGE_ITSELF);
        }
    }

    public static void checkingForInsertion(ExchangeRateDao instanceExchangeRate, CurrencyDto currencyDtoBase,
                                            CurrencyDto currencyDtoTarget) {
        selfCheck(currencyDtoBase.getCode(), currencyDtoTarget.getCode());

        if (instanceExchangeRate.rateIsExist(currencyDtoBase, currencyDtoTarget)) {
            throw new AlreadyExistException(EXIST_RATE + currencyDtoBase.getCode() + " " + currencyDtoTarget.getCode());
        }

        if (instanceExchangeRate.rateIsExist(currencyDtoTarget, currencyDtoBase)) {
            throw new AlreadyExistException(EXIST_REVERSE_RATE + currencyDtoTarget.getCode() + SEPARATOR + currencyDtoBase.getCode());
        }

        if ((instanceExchangeRate.rateIsExist(USD_DTO, currencyDtoBase) || instanceExchangeRate.rateIsExist(currencyDtoBase, USD_DTO)) &&
            (instanceExchangeRate.rateIsExist(USD_DTO, currencyDtoTarget) || instanceExchangeRate.rateIsExist(currencyDtoTarget, USD_DTO))) {
            throw new AlreadyExistException(EXIST_CROSS_RATE + USD_DTO.getCode());
        }
    }
}
