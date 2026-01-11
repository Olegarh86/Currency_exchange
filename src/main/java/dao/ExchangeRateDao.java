package dao;

import dao.util.DBConnector;
import dto.*;
import exception.DaoException;
import exception.NotFoundException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExchangeRateDao {
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

    public void save(ExchangeRateRequestDto exchangeRateRequestDto) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, exchangeRateRequestDto.baseCurrency().id());
            preparedStatement.setObject(2, exchangeRateRequestDto.targetCurrency().id());
            preparedStatement.setBigDecimal(3, exchangeRateRequestDto.rate());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Exchange rate not saved: " + e.getMessage());
        }
    }

    public void updateRate(int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_RATE_SQL)) {
            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setInt(2, baseCurrencyId);
            preparedStatement.setInt(3, targetCurrencyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public List<ExchangeRateResponseDto> findAll() {
        List<ExchangeRateResponseDto> allRatesDto = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ExchangeRateDaoDto exchangeRateDaoDto = buildExchangeRateResponseDto(resultSet);
                CurrenciesResponseDto baseDto = currencyDao.findById(exchangeRateDaoDto.baseCurrencyId());
                CurrenciesResponseDto targetDto = currencyDao.findById(exchangeRateDaoDto.targetCurrencyId());
                allRatesDto.add(new ExchangeRateResponseDto(exchangeRateDaoDto.id(), baseDto, targetDto,
                        exchangeRateDaoDto.rate()));
            }
            return allRatesDto;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public ExchangeRateResponseDto findRateByCodes(String baseCode, String targetCode) {
        int baseId = currencyDao.findIdByCode(baseCode);
        int targetId = currencyDao.findIdByCode(targetCode);
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_EXCHANGE_RATE_BY_CODES_SQL)) {
            preparedStatement.setInt(1, baseId);
            preparedStatement.setInt(2, targetId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ExchangeRateDaoDto exchangeRateDaoDto = buildExchangeRateResponseDto(resultSet);
                CurrenciesResponseDto baseDto = currencyDao.findById(exchangeRateDaoDto.baseCurrencyId());
                CurrenciesResponseDto targetDto = currencyDao.findById(exchangeRateDaoDto.targetCurrencyId());
                return new ExchangeRateResponseDto(exchangeRateDaoDto.id(), baseDto, targetDto, exchangeRateDaoDto.rate());
            }
            throw new NotFoundException("There is no such course");
        } catch (SQLException | NoSuchElementException e) {
            throw new DaoException(e.getMessage());
        }
    }

    private static ExchangeRateDaoDto buildExchangeRateResponseDto(ResultSet resultSet) throws SQLException {
        return new ExchangeRateDaoDto(resultSet.getInt("id"),
                resultSet.getInt("base_currency_id"),
                resultSet.getInt("target_currency_id"),
                resultSet.getBigDecimal("rate"));
    }

    public boolean rateIsExist(String baseCode, String targetCode) {
        try {
            findRateByCodes(baseCode, targetCode);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
