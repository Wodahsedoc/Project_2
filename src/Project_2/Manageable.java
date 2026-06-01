
package Project_2;

import java.util.List;

/**
 *
 * @author aneirinblosch
 */
public interface Manageable<T> {

    void add(T entity);

    boolean update(T entity);

    boolean delete(String id);

    T findById(String id);

    List<T> getAll();
}