package dao;

import lombok.extern.slf4j.Slf4j;
import model.Currency;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JdbcExchangeRateDao extends BaseDaoImpl<ExchangeRate> {
    private static final String ID = "id";
    private static final String BASE_ID = "base_id";
    private static final String TARGET_ID = "target_id";
    private static final String BASE_FULL_NAME = "base_full_name";
    private static final String TARGET_FULL_NAME = "target_full_name";
    private static final String BASE_CODE = "base_code";
    private static final String TARGET_CODE = "target_code";
    private static final String BASE_SIGN = "base_sign";
    private static final String TARGET_SIGN = "target_sign";
    private static final String RATE = "rate";
    private static final String INSERT_SQL = """
            INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            RETURNING id
            """;
    private static final String UPDATE_RATE_SQL = """
            UPDATE exchange_rate
            SET rate = ?
            WHERE base_currency_id = ? and target_currency_id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT e.id,
                   e.base_currency_id,
                   e.target_currency_id,
                   e.rate,
                   base.id as base_id,
                   base.code as base_code,
                   base.full_name as base_full_name,
                   base.sign as base_sign,
                   target.id as target_id,
                   target.code as target_code,
                   target.full_name as target_full_name,
                   target.sign as target_sign
            FROM exchange_rate e
            LEFT JOIN currency base ON e.base_currency_id = base.id
            LEFT JOIN currency target ON e.target_currency_id = target.id
            """;
    private static final String FIND_ALL_EXCHANGE_RATES_SQL = FIND_ALL_SQL + """
            WHERE base.code = 'USD' AND target.code = ? OR
                  base.code = 'USD' AND target.code = ? OR
                  base.code = ? AND target.code = 'USD' OR
                  base.code = ? AND target.code = 'USD' OR
                  base.code = ? AND target.code = ? OR
                  base.code = ? AND target.code = ?
            """;

    @Override
    public Optional<List<ExchangeRate>> findAll() {
        return executeFindAll(FIND_ALL_SQL);
    }

    @Override
    public Long save(ExchangeRate exchangeRate) {
        return executeSave(INSERT_SQL,
                exchangeRate.baseCurrency().id(),
                exchangeRate.targetCurrency().id(),
                exchangeRate.rate());
    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) {
        return executeFindByCodes(FIND_ALL_EXCHANGE_RATES_SQL, baseCode, targetCode);
    }

    public void updateRate(ExchangeRate exchangeRate) {
        executeUpdate(UPDATE_RATE_SQL,
                exchangeRate.rate(),
                exchangeRate.baseCurrency().id(),
                exchangeRate.targetCurrency().id());
    }

    @Override
    public ExchangeRate buildEntity(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong(ID);

        Currency baseCurrencyResult = new Currency(
                resultSet.getLong(BASE_ID),
                resultSet.getString(BASE_FULL_NAME),
                resultSet.getString(BASE_CODE),
                resultSet.getString(BASE_SIGN));

        Currency targetCurrencyResult = new Currency(
                resultSet.getLong(TARGET_ID),
                resultSet.getString(TARGET_FULL_NAME),
                resultSet.getString(TARGET_CODE),
                resultSet.getString(TARGET_SIGN));

        BigDecimal rate = resultSet.getBigDecimal(RATE);

        return new ExchangeRate(id, baseCurrencyResult, targetCurrencyResult, rate);
    }

    public Optional<List<ExchangeRate>> findAllExchangeRates(String baseCode, String targetCode) {
        return executeFindAllRates(FIND_ALL_EXCHANGE_RATES_SQL, baseCode, targetCode, baseCode, targetCode, baseCode,
                targetCode, targetCode, baseCode);
    }
}
