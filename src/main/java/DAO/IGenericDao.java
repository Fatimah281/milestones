package DAO;
//<editor-fold desc="Imports">
import java.util.List;
import java.util.Optional;
//</editor-fold>

public interface IGenericDao<T, ID> {

    //<editor-fold desc="Methods">
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll(int offset, int limit);

    T update(T entity);

    boolean deleteById(ID id);
    //</editor-fold>
}
