package dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T> {
    Long save(T entity);
    Optional<List<T>> findAll();
}
