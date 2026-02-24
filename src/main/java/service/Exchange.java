package service;

import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.*;
import exception.NotFoundException;
import exception.TooMuchExchangeRates;
import lombok.extern.slf4j.Slf4j;
import model.Currency;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
public class Exchange implements Service {
    private static final String USD = "USD";
    private static final int SCALE_RESULT = 2;
    private static final int SCALE = 6;
    private final JdbcCurrencyDao jdbcCurrencyDao;
    private final JdbcExchangeRateDao jdbcExchangeRateDao;

    public Exchange(JdbcCurrencyDao jdbcCurrencyDao, JdbcExchangeRateDao jdbcExchangeRateDao) {
        this.jdbcCurrencyDao = jdbcCurrencyDao;
        this.jdbcExchangeRateDao = jdbcExchangeRateDao;
    }

    @Override
    public ExchangeDto convert(String baseCode, String targetCode, BigDecimal amount) {
        List<ExchangeRate> allExchangeRates = jdbcExchangeRateDao.findAllExchangeRates(baseCode,
                targetCode).orElseThrow(() -> new NotFoundException(baseCode + targetCode));
        CurrenciesWithRateDto rateDto = getRate(baseCode, targetCode, allExchangeRates);
        BigDecimal convertedAmount = amount.multiply(rateDto.rate()).setScale(SCALE_RESULT, RoundingMode.HALF_EVEN);
        return new ExchangeDto(rateDto.baseCurrency(), rateDto.targetCurrency(), rateDto.rate(), amount, convertedAmount);
    }

    private static CurrenciesWithRateDto getRate(String baseCode, String targetCode, List<ExchangeRate> allExchangeRates) {
        BigDecimal rate = null;
        Currency baseCurrency = null;
        Currency targetCurrency = null;
        if (allExchangeRates.size() == 1) {
            ExchangeRate exchangeRate = allExchangeRates.get(0);
            if (exchangeRate.baseCurrency().code().equals(baseCode) && exchangeRate.targetCurrency().code().equals(targetCode)) {
                rate = exchangeRate.rate();
                baseCurrency = exchangeRate.baseCurrency();
                targetCurrency = exchangeRate.targetCurrency();
            }
            if (exchangeRate.baseCurrency().code().equals(targetCode) && exchangeRate.targetCurrency().code().equals(baseCode)) {
                rate = BigDecimal.ONE.divide(exchangeRate.rate(), SCALE, RoundingMode.HALF_EVEN);
                baseCurrency = exchangeRate.targetCurrency();
                targetCurrency = exchangeRate.baseCurrency();
            }
        } else if (allExchangeRates.size() == 2) {
            ExchangeRate firstExchangeRate;
            ExchangeRate secondExchangeRate;
            if (allExchangeRates.get(0).baseCurrency().code().equals(targetCode) ||
                allExchangeRates.get(0).targetCurrency().code().equals(targetCode)) {
                firstExchangeRate = allExchangeRates.get(1);
                secondExchangeRate = allExchangeRates.get(0);
            } else {
                firstExchangeRate = allExchangeRates.get(0);
                secondExchangeRate = allExchangeRates.get(1);
            }
            if ((firstExchangeRate.baseCurrency().code().equals(baseCode) && firstExchangeRate.targetCurrency().code().equals(USD)) &&
                (secondExchangeRate.baseCurrency().code().equals(targetCode) && secondExchangeRate.targetCurrency().code().equals(USD))) {
                rate = firstExchangeRate.rate().divide(secondExchangeRate.rate(), SCALE, RoundingMode.HALF_EVEN);
                baseCurrency = firstExchangeRate.baseCurrency();
                targetCurrency = secondExchangeRate.baseCurrency();
            }
            if ((firstExchangeRate.baseCurrency().code().equals(USD) && firstExchangeRate.targetCurrency().code().equals(baseCode)) &&
                (secondExchangeRate.baseCurrency().code().equals(USD) && secondExchangeRate.targetCurrency().code().equals(targetCode))) {
                rate = secondExchangeRate.rate().divide(firstExchangeRate.rate(), SCALE, RoundingMode.HALF_EVEN);
                baseCurrency = firstExchangeRate.targetCurrency();
                targetCurrency = secondExchangeRate.targetCurrency();
            }
            if ((firstExchangeRate.baseCurrency().code().equals(baseCode) && firstExchangeRate.targetCurrency().code().equals(USD)) &&
                (secondExchangeRate.baseCurrency().code().equals(USD) && secondExchangeRate.targetCurrency().code().equals(targetCode))) {
                rate = firstExchangeRate.rate().multiply(secondExchangeRate.rate()).setScale(SCALE, RoundingMode.HALF_EVEN);
                baseCurrency = firstExchangeRate.baseCurrency();
                targetCurrency = secondExchangeRate.targetCurrency();
            }
            if ((firstExchangeRate.baseCurrency().code().equals(USD) && firstExchangeRate.targetCurrency().code().equals(baseCode)) &&
                (secondExchangeRate.baseCurrency().code().equals(targetCode) && secondExchangeRate.targetCurrency().code().equals(USD))) {
                rate = BigDecimal.ONE.divide(firstExchangeRate.rate().multiply(secondExchangeRate.rate())).setScale(SCALE,
                        RoundingMode.HALF_EVEN);
                baseCurrency = firstExchangeRate.targetCurrency();
                targetCurrency = secondExchangeRate.baseCurrency();
            }
        } else {
            throw new TooMuchExchangeRates(baseCode + targetCode);
        }
        return new CurrenciesWithRateDto(baseCurrency, targetCurrency, rate);
    }
}