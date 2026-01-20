package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.Codes;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class UpdateRate {
    private final CurrencyDao instanceCurrency;
    private final ExchangeRateDao instanceExchangeRate;
    private final Codes codes;
    private final BigDecimal amount;

    public UpdateRate(CurrencyDao instanceCurrency, ExchangeRateDao exchangeRateDao, Codes codes, BigDecimal amount) {
        this.instanceCurrency = instanceCurrency;
        this.instanceExchangeRate = exchangeRateDao;
        this.codes = codes;
        this.amount = amount;
    }

    public void update() {
        CurrenciesResponseDto baseDto;
        CurrenciesResponseDto targetDto;
        try {
            baseDto = instanceCurrency.findCurrencyByCode(codes.baseCode());
            targetDto = instanceCurrency.findCurrencyByCode(codes.targetCode());
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }

        try {
            instanceExchangeRate.updateRate(baseDto.id(), targetDto.id(), amount);
        } catch (DaoException e) {
            log.error("Failed to process update rate {}", instanceExchangeRate);
            throw new NotFoundException(e.getMessage());
        }
    }

}
