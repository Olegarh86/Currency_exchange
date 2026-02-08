package dao;

import exception.AlreadyExistException;
import exception.DaoException;
import lombok.extern.slf4j.Slf4j;
import model.Currency;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
public class JdbcCurrencyDao implements CurrencyDao {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String FULL_NAME = "full_name";
    private static final String SIGN = "sign";
    private static final String CURRENCY_NOT_FOUND = " currency not found ";
    private static final String CURRENCY = " currency";
    private static final String INSERT_SQL = """
            INSERT INTO currency (code, full_name, sign)
            VALUES (?, ?, ?)
            RETURNING id
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currency
            """;
    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + " WHERE code = ?";
    private final DataSource dataSource;

    public JdbcCurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(Currency currency) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setString(1, currency.code());
            preparedStatement.setString(2, currency.name());
            preparedStatement.setString(3, currency.sign());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new AlreadyExistException(currency.code() + CURRENCY);
        }
    }

    @Override
    public Optional<List<Currency>> findAll() {
        List<Currency> currencies = new ArrayList<>();

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet));
            }
            return Optional.of(currencies);
        } catch (SQLException e) {
            throw new DaoException(CURRENCY_NOT_FOUND + e.getMessage());
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(buildCurrency(resultSet));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException(code + CURRENCY_NOT_FOUND);
        }
    }

    private static Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return new Currency((long) resultSet.getInt(ID), resultSet.getString(FULL_NAME), resultSet.getString(CODE),
                resultSet.getString(SIGN));
    }
}
