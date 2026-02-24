package dao;

import exception.AlreadyExistException;
import exception.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static AppContextListener.AppContextListener.getConnection;

public abstract class BaseDaoImpl<T> implements CrudDao<T> {
    private static final String NOT_SAVED = " not saved";
    private static final String NOT_FOUND = "Entities not found ";
    private static final String NOT_UPDATED = " not updates";

    public abstract T buildEntity(ResultSet resultSet) throws SQLException;

    protected Optional<List<T>> executeFindAll(String sql) {
        List<T> currencies = new ArrayList<>();

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(buildEntity(resultSet));
            }
        } catch (SQLException e) {
            throw new NotFoundException(NOT_FOUND);
        }
        return Optional.of(currencies);
    }

    protected Long executeSave(String sql, Object... params) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new AlreadyExistException(Arrays.toString(params) + NOT_SAVED);
        }
    }

    protected Optional<T> executeFindByCodes(String sql, Object... params) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(buildEntity(resultSet));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void executeUpdate(String sql, Object... params) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new NotFoundException(Arrays.toString(params) + NOT_UPDATED);
        }
    }

    protected Optional<List<T>> executeFindAllRates(String sql, Object... params) {
        List <T> entities = new ArrayList<>();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                entities.add(buildEntity(resultSet));
            }
            return Optional.of(entities);
        } catch (SQLException e) {
            throw new NotFoundException(Arrays.toString(params) + NOT_FOUND);
        }
    }
}
