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
    private final CurrencyDao  currencyDao;
    private final ExchangeRateDao exchangeRateDao;

    public Exchange(CurrencyDao  currencyDao, ExchangeRateDao exchangeRateDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeResponseDto convert(String baseCode, String targetCode, BigDecimal amount) {
        boolean rateIsExist = exchangeRateDao.rateIsExist(baseCode, targetCode);
        boolean reverseRateIsExist = exchangeRateDao.rateIsExist(targetCode, baseCode);
        boolean crossRateIsExist = (exchangeRateDao.rateIsExist(CODE_USD, baseCode) &&
                                    exchangeRateDao.rateIsExist(CODE_USD, targetCode));
        BigDecimal rate = null;
        ExchangeRateResponseDto exchangeRateDto;

        if (baseCode.equals(targetCode)) {
            rate = BigDecimal.valueOf(1);
        }

        if (rateIsExist) {
            exchangeRateDto = exchangeRateDao.findRateByCodes(baseCode, targetCode);
            rate = exchangeRateDto.rate();
        } else if (reverseRateIsExist) {
            exchangeRateDto = exchangeRateDao.findRateByCodes(targetCode, baseCode);
            rate = BigDecimal.ONE.divide(exchangeRateDto.rate(), 6, RoundingMode.HALF_UP);
        } else if (crossRateIsExist) {
            ExchangeRateResponseDto exchangeRateFrom = exchangeRateDao.findRateByCodes(CODE_USD, baseCode);
            ExchangeRateResponseDto exchangeRateTo = exchangeRateDao.findRateByCodes(CODE_USD, targetCode);
            rate = exchangeRateTo.rate().divide(exchangeRateFrom.rate(), 6, RoundingMode.HALF_EVEN);
        }
        CurrenciesResponseDto baseCurrency = currencyDao.findCurrencyByCode(baseCode);
        CurrenciesResponseDto targetCurrency = currencyDao.findCurrencyByCode(targetCode);

        if (rate == null) {
            throw  new NotFoundException("Exchange rate not found. Add exchange rate and try again.");
        }
        BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
        return new ExchangeResponseDto(baseCurrency, targetCurrency, rate, amount, result);
    }
}
