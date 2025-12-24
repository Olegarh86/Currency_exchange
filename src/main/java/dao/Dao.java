package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    E save(Connection connection, E currency);

    void update(Connection connection, E currency);

    E findById(Connection connection, K id);

    List<E> findAll(Connection connection);
}
