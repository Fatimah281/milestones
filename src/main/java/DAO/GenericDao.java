package DAO;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
//</editor-fold>

public class GenericDao<T, ID> implements IGenericDao<T, ID> {

    //<editor-fold desc="Fields">
    protected final EntityManager em;
    private final Class<T> entityClass;
    private final Function<T, ID> idGetter;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public GenericDao(EntityManager em, Class<T> entityClass, Function<T, ID> idGetter) {
        this.em = em;
        this.entityClass = entityClass;
        this.idGetter = idGetter;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    @Override
    public T save(T entity) {
        if (idGetter.apply(entity) == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll(int offset, int limit) {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id", entityClass)
            .setFirstResult(Math.max(0, offset))
            .setMaxResults(Math.max(1, limit))
            .getResultList();
    }

    @Override
    public T update(T entity) {
        return em.merge(entity);
    }

    @Override
    public boolean deleteById(ID id) {
        return findById(id).map(e -> {
            em.remove(em.contains(e) ? e : em.merge(e));
            return true;
        }).orElse(false);
    }
    //</editor-fold>
}
