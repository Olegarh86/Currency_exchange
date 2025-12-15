package dao;

import dto.CurrenciesFilter;
import model.Currency;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.util.List;
import java.util.Optional;

public class DaoRunner {
    public static void main(String[] args) {
        CurrenciesFilter filter = new CurrenciesFilter("USD", "RUB");
        ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
        CurrencyDao currencyDao = CurrencyDao.getInstance();
        List<ExchangeRate> all = exchangeRateDao.findAll(filter);
        CurrencyDao instance = CurrencyDao.getInstance();
        for (ExchangeRate exchangeRate : all) {
            System.out.println(exchangeRate);
            System.out.println(instance.findById(exchangeRate.getBaseCurrencyId()));
            System.out.println(instance.findById(exchangeRate.getTargetCurrencyId()));
        }
        System.out.println(" ");
        int CurrentCurrency = currencyDao.findCurrencyIdByCode("RUB");
        Optional<Currency> optionalCurrency = currencyDao.findById(CurrentCurrency);
        System.out.println(optionalCurrency);

//        exchangeRateDao.save(new ExchangeRate(instance.findCurrencyIdByCode(filter.base_currency_code()),
//                instance.findCurrencyIdByCode(filter.target_currency_code()), new BigDecimal("0.01")));
    }
}
