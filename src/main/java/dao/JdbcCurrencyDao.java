package dao;

import lombok.extern.slf4j.Slf4j;
import model.Currency;

import java.sql.*;
import java.util.List;
import java.util.Optional;


@Slf4j
public class JdbcCurrencyDao extends BaseDaoImpl<Currency> {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String FULL_NAME = "full_name";
    private static final String SIGN = "sign";
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

    @Override
    public Long save(Currency currency) {
        return executeSave(INSERT_SQL, currency.code(), currency.name(), currency.sign());
    }

    @Override
    public Optional<List<Currency>> findAll() {
        return executeFindAll(FIND_ALL_SQL);
    }

    public Optional<Currency> findByCode(String code) {
        return executeFindByCodes(FIND_BY_CODE_SQL, code);
    }

    @Override
    public Currency buildEntity(ResultSet resultSet) throws SQLException {
        return new Currency(resultSet.getLong(ID), resultSet.getString(FULL_NAME), resultSet.getString(CODE),
                resultSet.getString(SIGN));
    }
}
