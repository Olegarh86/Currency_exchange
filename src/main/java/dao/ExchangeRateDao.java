package dao;

import dao.util.DBConnector;
import dto.FindExchangeRateByIdDto;
import exception.DaoException;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExchangeRateDao implements Dao<Integer, ExchangeRate> {
    private final static ExchangeRateDao INSTANCE = new ExchangeRateDao();
    private static final String INSERT_SQL = """
            INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) 
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE exchange_rate
            SET base_currency_id = ?, target_currency_id = ?,  rate = ?
            WHERE id = ?
            """;
    private static final String UPDATE_RATE_SQL = """
            UPDATE exchange_rate
            SET rate = ?
            WHERE base_currency_id = ? and target_currency_id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT e.id, base_currency_id, target_currency_id, rate, c.id, c.code, c.full_name, c.sign
            FROM exchange_rate e
            LEFT JOIN currency c ON e.base_currency_id = c.id
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE id = ?";
    private static final String FIND_EXCHANGE_RATE_BY_CODES_SQL = FIND_ALL_SQL + """
             Where base_currency_id = ? AND target_currency_id = ?
            """;
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final DBConnector connector = new DBConnector();
    private final Connection connection = connector.getConnection();

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setObject(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                exchangeRate.setId(resultSet.getInt(1));
                return exchangeRate;
            }
            throw new DaoException("Обменный курс не сохранен");
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(ExchangeRate exchangeRate) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.setInt(4, exchangeRate.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void updateRate(int baseCurrencyId, int targetCurrencyId,  BigDecimal rate) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_RATE_SQL)) {
            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setInt(2, baseCurrencyId);
            preparedStatement.setInt(3, targetCurrencyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public ExchangeRate findById(Integer id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return buildExchangeRate(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public ExchangeRate findRate(FindExchangeRateByIdDto filter) {
        List<Integer> parameters = new ArrayList<>();
        ExchangeRate exchangeRate = null;
        int baseCode = currencyDao.findIdByCode(filter.base_currency_code());
        int targetCode = currencyDao.findIdByCode(filter.target_currency_code());
        parameters.add(baseCode);
        parameters.add(targetCode);
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_EXCHANGE_RATE_BY_CODES_SQL)) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setInt(i + 1, parameters.get(i));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                exchangeRate = buildExchangeRate(resultSet);
                return exchangeRate;
            } else {
                throw new DaoException("Такого курса не существует");
            }
        } catch (SQLException | NoSuchElementException e) {
            throw new DaoException(e);
        }
    }

    private static ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        ExchangeRate exchangeRate = new ExchangeRate(resultSet.getInt("id"),
                resultSet.getInt("base_currency_id"),
                resultSet.getInt("target_currency_id"),
                resultSet.getBigDecimal("rate"));
        return exchangeRate;

    }
}
