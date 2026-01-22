package dao;

import dto.CurrencyDto;
import exception.DaoException;
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
    private static final String CURRENCY_NOT_FOUND = " currency not found";
    private static final String CURRENCY_ALREADY_EXIST = " currency already exist";
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

    public void save(CurrencyDto currencyDto) {

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setString(1, currencyDto.getCode());
            preparedStatement.setString(2, currencyDto.getName());
            preparedStatement.setString(3, currencyDto.getSign());
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
                Currency currency = buildCurrency(resultSet.getInt(ID), resultSet);
                currencies.add(CurrencyMapper.INSTANCE.convertCurrencyToDto(currency));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(CURRENCY_NOT_FOUND + e.getMessage());
        }
    }

    public CurrencyDto findCurrencyByCode(CurrencyDto currencyDto) {

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_BY_CODE_SQL)) {
            Currency currency = null;
            preparedStatement.setString(1, currencyDto.getCode());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currency = new Currency(resultSet.getInt(ID), resultSet.getString(FULL_NAME),
                        resultSet.getString(CODE), resultSet.getString(SIGN));
            }
            return CurrencyMapper.INSTANCE.convertCurrencyToDto(currency);
        } catch (SQLException e) {
            throw new DaoException(currencyDto.getCode() + CURRENCY_NOT_FOUND);
        }
    }

    private static Currency buildCurrency(Integer id, ResultSet resultSet) throws SQLException {
        return new Currency(id, resultSet.getString(FULL_NAME), resultSet.getString(CODE), resultSet.getString(SIGN));

    }
}
