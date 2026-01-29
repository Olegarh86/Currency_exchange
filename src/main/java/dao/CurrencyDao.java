package dao;

import dto.CurrencyDto;
import exception.DaoException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class CurrencyDao {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String FULL_NAME = "full_name";
    private static final String SIGN = "sign";
    private static final String CURRENCY_NOT_FOUND = " currency not found ";
    private static final String CURRENCY_ALREADY_EXIST = " currency already exist";
    private static final String INSERT_SQL = """
            INSERT INTO currency (code, full_name, sign)
            VALUES (?, ?, ?)
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currency
            """;
    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + " WHERE code = ?";
    private final DataSource dataSource;

    public CurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(CurrencyDto currencyDto) {
        Currency currency = CurrencyMapper.INSTANCE.dtoToCurrency(currencyDto);

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setString(1, currency.code());
            preparedStatement.setString(2, currency.name());
            preparedStatement.setString(3, currency.sign());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(currencyDto.getCode() + CURRENCY_ALREADY_EXIST);
        }
    }

    public List<CurrencyDto> findAll() {
        List<CurrencyDto> currencies = new ArrayList<>();

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Currency currency = buildCurrency(resultSet);
                currencies.add(CurrencyMapper.INSTANCE.currencyToDto(currency));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(CURRENCY_NOT_FOUND + e.getMessage());
        }
    }

    public CurrencyDto findCurrencyByCode(CurrencyDto currencyDto) {
        Currency currency = CurrencyMapper.INSTANCE.dtoToCurrency(currencyDto);

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, currency.code());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Currency currencyResult = buildCurrency(resultSet);
                return CurrencyMapper.INSTANCE.currencyToDto(currencyResult);
            }
            throw new NotFoundException(CURRENCY_NOT_FOUND + currency.code());
        } catch (SQLException e) {
            throw new DaoException(currencyDto.getCode() + CURRENCY_NOT_FOUND);
        }
    }

    private static Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(resultSet.getInt(ID), resultSet.getString(FULL_NAME), resultSet.getString(CODE),
                resultSet.getString(SIGN));
    }
}
