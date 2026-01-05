package dao;

import dao.util.DBConnector;
import exception.DaoException;
import model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CurrencyDao implements Dao<Integer, Currency> {
    private static final CurrencyDao INSTANCE = new CurrencyDao();
    private static final String INSERT_SQL = """
            INSERT INTO currency (code, full_name, sign) 
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE currency
            SET code = ?, full_name = ?, sign = ?
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currency
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE id = ?";
    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + " WHERE code = ?";
    private final DBConnector connector = new DBConnector();
    private final Connection connection = connector.getConnection();

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Currency save(Currency currency) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                currency.setId(rs.getInt(1));
            }
            return currency;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Currency currency) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.setInt(4, currency.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }


    @Override
    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet.getInt("id"), resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Currency findById(Integer id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return buildCurrency(id, resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Currency buildCurrency(Integer id, ResultSet resultSet) throws SQLException {
        return new Currency(id,
                resultSet.getString("full_name"),
                resultSet.getString("code"),
                resultSet.getString("sign"));
    }

    public int findIdByCode(String code) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
            throw new DaoException(code + " Валюта не найдена");
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
