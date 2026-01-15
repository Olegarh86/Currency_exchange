package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.Codes;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import exception.NotFoundException;

import java.math.BigDecimal;

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
            throw new NotFoundException(e.getMessage());
        }
    }

}
