package dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<E> {
    Long save(E entity);
    Optional<List<E>> findAll();
}
