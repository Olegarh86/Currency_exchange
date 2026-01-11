package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.ExchangeRateResponseDto;
import dto.CurrenciesResponseDto;
import dto.ExchangeResponseDto;
import exception.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Exchange {
    private static final String CODE_USD = "USD";

    public ExchangeResponseDto convert(String baseCode, String targetCode, BigDecimal amount) {
        CurrencyDao instanceCurrency = CurrencyDao.getInstance();
        ExchangeRateDao instanceExchangeRate = ExchangeRateDao.getInstance();
        boolean rateIsExist = instanceExchangeRate.rateIsExist(baseCode, targetCode);
        boolean reverseRateIsExist = instanceExchangeRate.rateIsExist(targetCode, baseCode);
        boolean crossRateIsExist = (instanceExchangeRate.rateIsExist(CODE_USD, baseCode) &&
                                    instanceExchangeRate.rateIsExist(CODE_USD, targetCode));
        BigDecimal rate = null;
        ExchangeRateResponseDto exchangeRateDto;

        if (baseCode.equals(targetCode)) {
            rate = BigDecimal.valueOf(1);
        }

        if (rateIsExist) {
            exchangeRateDto = instanceExchangeRate.findRateByCodes(baseCode, targetCode);
            rate = exchangeRateDto.rate();
        } else if (reverseRateIsExist) {
            exchangeRateDto = instanceExchangeRate.findRateByCodes(targetCode, baseCode);
            rate = BigDecimal.ONE.divide(exchangeRateDto.rate(), 6, RoundingMode.HALF_UP);
        } else if (crossRateIsExist) {
            ExchangeRateResponseDto exchangeRateFrom = instanceExchangeRate.findRateByCodes(CODE_USD, baseCode);
            ExchangeRateResponseDto exchangeRateTo = instanceExchangeRate.findRateByCodes(CODE_USD, targetCode);
            rate = exchangeRateTo.rate().divide(exchangeRateFrom.rate(), 6, RoundingMode.HALF_EVEN);
        }
        CurrenciesResponseDto baseCurrency = instanceCurrency.findCurrencyByCode(baseCode);
        CurrenciesResponseDto targetCurrency = instanceCurrency.findCurrencyByCode(targetCode);

        if (rate == null) {
            throw  new NotFoundException("Exchange rate not found. Add exchange rate and try again.");
        }
        BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
        return new ExchangeResponseDto(baseCurrency, targetCurrency, rate, amount, result);
    }
}
