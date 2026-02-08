package dao;

import exception.DaoException;
import lombok.extern.slf4j.Slf4j;
import model.Currency;
import model.ExchangeRate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
public class JdbcExchangeRateDao implements ExchangeRateDao {
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
    private static final String EXCHANGE_RATE_NOT_SAVED = "Exchange rate not saved: ";
    private static final String EXCHANGE_RATE_NOT_UPDATED = "Exchange rate not updates: ";
    private static final String THERE_IS_NO_SUCH_COURSE = "There is no such course";

    private final DataSource dataSource;
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
                   bc.id as base_id,
                   bc.code as base_code,
                   bc.full_name as base_full_name,
                   bc.sign as base_sign,
                   tc.id as target_id,
                   tc.code as target_code,
                   tc.full_name as target_full_name,
                   tc.sign as target_sign
            FROM exchange_rate e
            LEFT JOIN currency bc ON e.base_currency_id = bc.id
            LEFT JOIN currency tc ON e.target_currency_id = tc.id
            """;

    private static final String FIND_EXCHANGE_RATE_BY_CODES_SQL = FIND_ALL_SQL + """
             Where bc.code = ? AND tc.code = ?
            """;

    public JdbcExchangeRateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(ExchangeRate exchangeRate) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setLong(1, exchangeRate.baseCurrency().id());
            preparedStatement.setLong(2, exchangeRate.targetCurrency().id());
            preparedStatement.setBigDecimal(3, exchangeRate.rate());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new DaoException(EXCHANGE_RATE_NOT_SAVED + e.getMessage());
        }
    }

    @Override
    public void updateRate(ExchangeRate exchangeRate) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(UPDATE_RATE_SQL)) {
            preparedStatement.setBigDecimal(1, exchangeRate.rate());
            preparedStatement.setLong(2, exchangeRate.baseCurrency().id());
            preparedStatement.setLong(3, exchangeRate.targetCurrency().id());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(EXCHANGE_RATE_NOT_UPDATED + e.getMessage());
        }
    }

    @Override
    public Optional<List<ExchangeRate>> findAll() {
        List<ExchangeRate> allRates = new ArrayList<>();

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                allRates.add(buildExchangeRate(resultSet));
            }
            return Optional.of(allRates);
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_EXCHANGE_RATE_BY_CODES_SQL)) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildExchangeRate(resultSet));
            }
            return Optional.empty();
        } catch (SQLException | NoSuchElementException e) {
            throw new DaoException(THERE_IS_NO_SUCH_COURSE + baseCode + targetCode + e.getMessage());
        }
    }

    private static ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
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
}
