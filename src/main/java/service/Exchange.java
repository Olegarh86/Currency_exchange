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
    private static final int SCALE = 6;
    private final CurrencyDao  currencyDao;
    private final ExchangeRateDao exchangeRateDao;

    public Exchange(CurrencyDao  currencyDao, ExchangeRateDao exchangeRateDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeResponseDto convert(CurrencyDto currencyDtoBase, CurrencyDto currencyDtoTarget, BigDecimal amount) {
        BigDecimal rate;
        ExchangeRateDto exchangeRateDto;

        try {
            exchangeRateDto = exchangeRateDao.findExchangeRate(currencyDtoBase, currencyDtoTarget);
            rate = exchangeRateDto.getRate();
        } catch (Exception e) {
            try {
                exchangeRateDto = exchangeRateDao.findExchangeRate(currencyDtoTarget, currencyDtoBase);
                rate = BigDecimal.ONE.divide(exchangeRateDto.getRate(), SCALE, RoundingMode.HALF_UP);
            } catch (Exception ex) {
                try {
                    ExchangeRateDto exchangeRateFrom = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoBase);
                    ExchangeRateDto exchangeRateTo = exchangeRateDao.findExchangeRate(USD_DTO, currencyDtoTarget);
                    rate = exchangeRateTo.getRate().divide(exchangeRateFrom.getRate(), SCALE, RoundingMode.HALF_EVEN);
                } catch (Exception exc) {
                    throw  new NotFoundException(RATE_NOT_FOUND + currencyDtoBase + currencyDtoTarget);
                }
            }
        }

        CurrencyDto baseCurrency = currencyDao.findCurrencyByCode(currencyDtoBase);
        CurrencyDto targetCurrency = currencyDao.findCurrencyByCode(currencyDtoTarget);

        BigDecimal result = amount.multiply(rate).setScale(SCALE, RoundingMode.HALF_EVEN);
        return new ExchangeResponseDto(baseCurrency, targetCurrency, rate, amount, result);
    }
}
