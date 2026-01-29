package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.*;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class Exchange {
    private static final CurrencyDto USD_DTO = new CurrencyRequestDto("USD");
    private static final String RATE_NOT_FOUND = "Exchange rate not found. Add exchange rate and try again.";
    private static final int SCALE_RESULT = 2;
    private static final int SCALE = 6;
    private final CurrencyDao currencyDao;
    private final ExchangeRateDao exchangeRateDao;

    public Exchange(CurrencyDao currencyDao, ExchangeRateDao exchangeRateDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeDto convert(CurrencyDto currencyDtoBase, CurrencyDto currencyDtoTarget, BigDecimal amount) {
        BigDecimal rate;
        ExchangeRateDto exchangeRateDto;
        ExchangeRateDto exchangeRateFrom;
        ExchangeRateDto exchangeRateTo;

        try {
            exchangeRateDto = exchangeRateDao.findExchangeRate(currencyDtoBase, currencyDtoTarget);
            rate = exchangeRateDto.getRate();
        } catch (Exception e) {
            try {
                exchangeRateDto = exchangeRateDao.findExchangeRate(currencyDtoTarget, currencyDtoBase);
                BigDecimal rateResult = exchangeRateDto.getRate();
                rate = BigDecimal.ONE.divide(rateResult, SCALE, RoundingMode.HALF_EVEN);
            } catch (Exception r) {
                try {
                    exchangeRateFrom = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoBase);
                    exchangeRateTo = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoTarget);
                } catch (Exception t) {
                    try {
                        exchangeRateFrom = exchangeRateDao.findExchangeRate(currencyDtoBase, USD_DTO);
                        exchangeRateTo = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoTarget);
                    } catch (Exception y) {
                        try {
                            exchangeRateFrom = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoBase);
                            exchangeRateTo = exchangeRateDao.findExchangeRate(currencyDtoTarget, USD_DTO);
                        } catch (Exception z) {
                            try {
                                exchangeRateFrom = exchangeRateDao.findExchangeRate(currencyDtoBase, USD_DTO);
                                exchangeRateTo = exchangeRateDao.findExchangeRate(currencyDtoTarget, USD_DTO);
                            } catch (Exception w) {
                                throw new NotFoundException(RATE_NOT_FOUND);
                            }
                        }
                    }
                }
                rate = exchangeRateTo.getRate().divide(exchangeRateFrom.getRate(), SCALE, RoundingMode.HALF_EVEN);
            }
        }
        CurrencyDto baseCurrency = currencyDao.findCurrencyByCode(currencyDtoBase);
        CurrencyDto targetCurrency = currencyDao.findCurrencyByCode(currencyDtoTarget);

        BigDecimal result = amount.multiply(rate).setScale(SCALE_RESULT, RoundingMode.HALF_EVEN);
        return new ExchangeDto(baseCurrency, targetCurrency, rate, amount, result);
    }
}
