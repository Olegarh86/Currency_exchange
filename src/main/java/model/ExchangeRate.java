package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dao.CurrencyDao;

import java.math.BigDecimal;

public class ExchangeRate {
    private int id;
    @JsonIgnore
    private int baseCurrencyId;
    @JsonIgnore
    private int targetCurrencyId;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;


    public ExchangeRate(int baseCurrencyId, int targetCurrencyId, Currency baseCurrency,
                        Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public ExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public ExchangeRate(int id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

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

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
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
}
