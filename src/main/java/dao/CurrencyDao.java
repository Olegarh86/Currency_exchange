package dao;

import dao.util.DBConnector;
import dto.CurrenciesRequestDto;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import mapper.CurrencyMapper;
import model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CurrencyDao {
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
    private final CurrencyMapper INSTANCE_MAPPER = CurrencyMapper.INSTANCE;

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    public void save(CurrenciesRequestDto currenciesRequestDto) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildCurrencyResponseDto(resultSet.getInt("id"), resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public CurrenciesResponseDto findById(Integer id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return buildCurrencyResponseDto(id, resultSet);
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }

    private static CurrenciesResponseDto buildCurrencyResponseDto(Integer id, ResultSet resultSet) throws SQLException {
        return CurrencyMapper.INSTANCE.convertCurrencyToDto(new Currency(id, resultSet.getString("full_name"),
                resultSet.getString("code"),
                resultSet.getString("sign")));
    }

    public CurrenciesResponseDto findCurrencyByCode(String currencyCode) {
        CurrenciesResponseDto currenciesResponseDto = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, currencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currenciesResponseDto = new CurrenciesResponseDto(resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("code"),
                        resultSet.getString("sign"));
            }
            return currenciesResponseDto;
        } catch (SQLException e) {
            throw new DaoException(currencyCode + " currency not found");
        }
    }

    public int findIdByCode(String currencyCode) {
        int id = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, currencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            return id;
        } catch (SQLException e) {
            throw new DaoException(e + currencyCode + " Валюта не найдена");
        }
    }
}
