package dao;

import dto.*;
import exception.DaoException;
import exception.NotFoundException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExchangeRateDao {
    private final DataSource dataSource;
    private static final String INSERT_SQL = """
            INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE_RATE_SQL = """
            UPDATE exchange_rate
            SET rate = ?
            WHERE base_currency_id = ? and target_currency_id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT e.id, e.base_currency_id, e.target_currency_id, e.rate,
                   bc.id as base_id, bc.code as base_code, bc.full_name as base_full_name, bc.sign as base_sign,
                   tc.id as target_id, tc.code as target_code, tc.full_name as target_full_name, tc.sign as target_sign
            FROM exchange_rate e
            LEFT JOIN currency bc ON e.base_currency_id = bc.id
            LEFT JOIN currency tc ON e.target_currency_id = tc.id
            """;

    private static final String FIND_EXCHANGE_RATE_BY_CODES_SQL = FIND_ALL_SQL + """
             Where bc.code = ? AND tc.code = ?
            """;

    public ExchangeRateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(ExchangeRateRequestDto exchangeRateRequestDto) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, exchangeRateRequestDto.baseCurrency().id());
            preparedStatement.setObject(2, exchangeRateRequestDto.targetCurrency().id());
            preparedStatement.setBigDecimal(3, exchangeRateRequestDto.rate());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Exchange rate not saved: " + e.getMessage());
        }
    }

    public void updateRate(int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(UPDATE_RATE_SQL)) {
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
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CurrenciesResponseDto baseDto = new CurrenciesResponseDto(resultSet.getInt("base_id"),
                        resultSet.getString("base_full_name"), resultSet.getString("base_code"),
                        resultSet.getString("base_sign"));
                CurrenciesResponseDto targetDto = new CurrenciesResponseDto(resultSet.getInt("target_id"),
                        resultSet.getString("target_full_name"), resultSet.getString("target_code"),
                        resultSet.getString("target_sign"));

                allRatesDto.add(new ExchangeRateResponseDto(resultSet.getInt("id"), baseDto, targetDto,
                        resultSet.getBigDecimal("rate")));
            }
            return allRatesDto;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public ExchangeRateResponseDto findRateByCodes(String baseCode, String targetCode) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_EXCHANGE_RATE_BY_CODES_SQL)) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                CurrenciesResponseDto baseDto = new CurrenciesResponseDto(resultSet.getInt("base_id"),
                        resultSet.getString("base_full_name"), resultSet.getString("base_code"),
                        resultSet.getString("base_sign"));
                CurrenciesResponseDto targetDto = new CurrenciesResponseDto(resultSet.getInt("target_id"),
                        resultSet.getString("target_full_name"), resultSet.getString("target_code"),
                        resultSet.getString("target_sign"));
                return new ExchangeRateResponseDto(resultSet.getInt("id"), baseDto, targetDto, resultSet.getBigDecimal("rate"));
            }
            throw new NotFoundException("There is no such course");
        } catch (SQLException | NoSuchElementException e) {
            throw new DaoException(e.getMessage());
        }
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
