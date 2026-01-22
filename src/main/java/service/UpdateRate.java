package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.*;
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
        CurrencyDto baseDto;
        CurrencyDto targetDto;
        try {
            CurrencyRequestDto currencyRequestDtoBase = new CurrencyRequestDto(codes.baseCode());
            CurrencyRequestDto currencyRequestDtoTarget = new CurrencyRequestDto(codes.targetCode());
            baseDto = instanceCurrency.findCurrencyByCode(currencyRequestDtoBase);
            targetDto = instanceCurrency.findCurrencyByCode(currencyRequestDtoTarget);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }

        try {
            ExchangeRateDto exchangeRateDto = new ExchangeRateRequestDto(baseDto, targetDto, amount);
            instanceExchangeRate.updateRate(exchangeRateDto);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

}
