package Project_2;

import java.util.List;

/**
 * @param <T> the domain entity type this DAO manages
 * @author Aneirin Blosch
 */
public interface DAO<T> {

    void insert(T entity);
    boolean update(T entity);
    boolean delete(String id);
    T findById(String id);
    List<T> findAll();
}
