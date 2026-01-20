package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.ExchangeRateResponseDto;
import dto.CurrenciesResponseDto;
import dto.ExchangeResponseDto;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class Exchange {
    private static final String CODE_USD = "USD";
    private static final String RATE_NOT_FOUND = "Exchange rate not found. Add exchange rate and try again.";
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
//            log.error("RATE_NOT_FOUND {} - {}", baseCode, targetCode );
            throw  new NotFoundException(RATE_NOT_FOUND + baseCode +  targetCode);
        }
        BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
        return new ExchangeResponseDto(baseCurrency, targetCurrency, rate, amount, result);
    }
}
