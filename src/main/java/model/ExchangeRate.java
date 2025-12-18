package model;

import dao.CurrencyDao;

import java.math.BigDecimal;

public class ExchangeRate {
    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;
    private final CurrencyDao currency = CurrencyDao.getInstance();

    public ExchangeRate(int id, int baseCurrency, int targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrency;
        this.targetCurrencyId = targetCurrency;
        this.rate = rate;
    }

    public ExchangeRate(int baseCurrency, int targetCurrency, BigDecimal rate) {
        this.baseCurrencyId = baseCurrency;
        this.targetCurrencyId = targetCurrency;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public int getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public void setTargetCurrencyId(int targetCurrencyId) {
        this.targetCurrencyId = targetCurrencyId;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "\n  id : " + id +
               ",\n  baseCurrency : " + currency.findById(baseCurrencyId).get() +
               ",\n  targetCurrency : " + currency.findById(targetCurrencyId).get() +
               ",\n  rate : " + rate;
    }
}
