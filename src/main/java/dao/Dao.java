package dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    E save(E currency);

    void update(E currency);

    E findById(K id);

    List<E> findAll();
}
