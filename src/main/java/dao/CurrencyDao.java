package dao;

import dto.CurrenciesRequestDto;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import mapper.CurrencyMapper;
import model.Currency;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CurrencyDao implements dao.Currency {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String FULL_NAME = "full_name";
    private static final String SIGN = "sign";
    private static final String CURRENCY_NOT_FOUND = " currency not found";
    private final DataSource dataSource;
    private static final String INSERT_SQL = """
            INSERT INTO currency (code, full_name, sign)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currency
            """;
    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + " WHERE code = ?";

    public CurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(CurrenciesRequestDto currenciesRequestDto) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setString(1, currenciesRequestDto.code());
            preparedStatement.setString(2, currenciesRequestDto.name());
            preparedStatement.setString(3, currenciesRequestDto.sign());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public List<CurrenciesResponseDto> findAll() {
        List<CurrenciesResponseDto> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildCurrencyResponseDto(resultSet.getInt(ID), resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    private static CurrenciesResponseDto buildCurrencyResponseDto(Integer id, ResultSet resultSet) throws SQLException {
        return CurrencyMapper.INSTANCE.convertCurrencyToDto(new Currency(id, resultSet.getString(FULL_NAME),
                resultSet.getString(CODE),
                resultSet.getString(SIGN)));
    }

    public CurrenciesResponseDto findCurrencyByCode(String currencyCode) {
        CurrenciesResponseDto currenciesResponseDto = null;
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, currencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currenciesResponseDto = new CurrenciesResponseDto(resultSet.getInt(ID),
                        resultSet.getString(FULL_NAME),
                        resultSet.getString(CODE),
                        resultSet.getString(SIGN));
            }
            return currenciesResponseDto;
        } catch (SQLException e) {
            throw new DaoException(currencyCode + CURRENCY_NOT_FOUND);
        }
    }

    @Override
    public List<CurrenciesResponseDto> getAllCurrencies() {
        List<CurrenciesResponseDto> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildCurrencyResponseDto(resultSet.getInt(ID), resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }
}
