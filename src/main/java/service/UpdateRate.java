package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.Codes;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import exception.NotFoundException;

import java.math.BigDecimal;

public class UpdateRate {
    private final ExchangeRateDao exchangeRateDao;
    private final Codes codes;
    private final BigDecimal amount;

    public UpdateRate(ExchangeRateDao exchangeRateDao, Codes codes, BigDecimal amount) {
        this.exchangeRateDao = exchangeRateDao;
        this.codes = codes;
        this.amount = amount;
    }

    public void update() {
        CurrencyDao currencyDao = CurrencyDao.getInstance();
        CurrenciesResponseDto baseDto;
        CurrenciesResponseDto targetDto;
        try {
            baseDto = currencyDao.findCurrencyByCode(codes.baseCode());
            targetDto = currencyDao.findCurrencyByCode(codes.targetCode());
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }

        try {
            exchangeRateDao.updateRate(baseDto.id(), targetDto.id(), amount);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

}
