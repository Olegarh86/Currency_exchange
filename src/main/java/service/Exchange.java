package service;

import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.*;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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
    public Optional<ExchangeDto> convert(String baseCode, String targetCode, BigDecimal amount) {
        Optional<ExchangeRate> mayBeExchangeRate = findExchangeRate(baseCode, targetCode);

        if (mayBeExchangeRate.isPresent()) {
            Currency baseCurrency = jdbcCurrencyDao.findByCode(baseCode).
                    orElseThrow(() -> new NotFoundException(baseCode));
            Currency targetCurrency = jdbcCurrencyDao.findByCode(targetCode).
                    orElseThrow(() -> new NotFoundException(targetCode));
            BigDecimal rate = mayBeExchangeRate.get().rate();

            BigDecimal convertedAmount = amount.multiply(rate).setScale(SCALE_RESULT,
                    RoundingMode.HALF_EVEN);
            return Optional.of(CurrencyMapper.INSTANCE.exchangeResultToDto(baseCurrency, targetCurrency, rate, amount,
                    convertedAmount));
        }
        return Optional.empty();

    }

    public Optional<ExchangeRate> findExchangeRate(String baseCode, String targetCode) {
        Optional<ExchangeRate> mayBeExchangeRate = findDirectExchangeRate(baseCode, targetCode);

        if (mayBeExchangeRate.isEmpty()) {
            mayBeExchangeRate = findReverseExchangeRate(baseCode, targetCode);
        }

        if (mayBeExchangeRate.isEmpty()) {
            mayBeExchangeRate = findExchangeRateThroughDollar(baseCode, targetCode);
        }
        return mayBeExchangeRate;
    }

    private Optional<ExchangeRate> findDirectExchangeRate(String baseCode, String targetCode) {
        return jdbcExchangeRateDao.findByCodes(baseCode, targetCode);
    }

    private Optional<ExchangeRate> findReverseExchangeRate(String baseCode, String targetCode) {
        Optional<ExchangeRate> mayBeExchangeRate = jdbcExchangeRateDao.findByCodes(targetCode, baseCode);

        if (mayBeExchangeRate.isPresent()) {
            ExchangeRate exchangeRate = mayBeExchangeRate.get();
            BigDecimal rate = exchangeRate.rate();
            BigDecimal rateResult = BigDecimal.ONE.divide(rate, SCALE, RoundingMode.HALF_EVEN);

            return Optional.of(
                    CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(
                            exchangeRate.baseCurrency(),
                            exchangeRate.targetCurrency(),
                            rateResult));
        }
        return Optional.empty();
    }

    private Optional<ExchangeRate> findExchangeRateThroughDollar(String baseCode, String targetCode) {
        BigDecimal rate;
        Optional<ExchangeRate> mayBeBaseUsd = findDirectExchangeRate(baseCode, USD);
        Optional<ExchangeRate> mayBeTargetUsd = findDirectExchangeRate(targetCode, USD);
        Optional<ExchangeRate> mayBeUsdBase = findReverseExchangeRate(baseCode, USD);
        Optional<ExchangeRate> mayBeUsdTarget = findReverseExchangeRate(targetCode, USD);

        if (mayBeBaseUsd.isPresent() && mayBeTargetUsd.isPresent()) {
            rate = mayBeTargetUsd.get().rate().divide(mayBeBaseUsd.get().rate(), SCALE, RoundingMode.HALF_EVEN);
            return Optional.of(CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(
                    mayBeBaseUsd.get().baseCurrency(),
                    mayBeTargetUsd.get().baseCurrency(),
                    rate));
        }

        if (mayBeUsdBase.isPresent() && mayBeUsdTarget.isPresent()) {
            rate = mayBeUsdBase.get().rate().divide(mayBeUsdTarget.get().rate(), SCALE, RoundingMode.HALF_EVEN);
            return Optional.of(CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(
                    mayBeUsdBase.get().targetCurrency(),
                    mayBeUsdTarget.get().targetCurrency(),
                    rate));
        }

        if (mayBeBaseUsd.isPresent() && mayBeUsdTarget.isPresent()) {
            rate = mayBeUsdTarget.get().rate().divide(mayBeBaseUsd.get().rate(), SCALE, RoundingMode.HALF_EVEN);
            return Optional.of(CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(
                    mayBeBaseUsd.get().baseCurrency(),
                    mayBeUsdTarget.get().targetCurrency(),
                    rate));
        }

        if (mayBeUsdBase.isPresent() && mayBeTargetUsd.isPresent()) {
            rate = mayBeTargetUsd.get().rate().divide(mayBeUsdBase.get().rate(), SCALE, RoundingMode.HALF_EVEN);
            return Optional.of(CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(
                    mayBeUsdBase.get().targetCurrency(),
                    mayBeTargetUsd.get().baseCurrency(),
                    rate));
        }
        return Optional.empty();
    }
}
