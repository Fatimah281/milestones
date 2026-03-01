package DAO;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
//</editor-fold>

public class GenericDao<T, ID> implements IGenericDao<T, ID> {

    //<editor-fold desc="Fields">
    private static final Logger LOG = LogManager.getLogger(GenericDao.class);
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
            LOG.trace("Persisting new entity: {}", entityClass.getSimpleName());
            em.persist(entity);
            return entity;
        }
        LOG.trace("Merging entity: {}", entityClass.getSimpleName());
        return em.merge(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        LOG.debug("Finding {} by id: {}", entityClass.getSimpleName(), id);
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll(int offset, int limit) {
        LOG.debug("Finding all {} with offset={}, limit={}", entityClass.getSimpleName(), offset, limit);
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id", entityClass)
            .setFirstResult(Math.max(0, offset))
            .setMaxResults(Math.max(1, limit))
            .getResultList();
    }

    @Override
    public T update(T entity) {
        LOG.trace("Updating entity: {}", entityClass.getSimpleName());
        return em.merge(entity);
    }

    @Override
    public boolean deleteById(ID id) {
        LOG.debug("Deleting {} by id: {}", entityClass.getSimpleName(), id);
        return findById(id).map(e -> {
            em.remove(em.contains(e) ? e : em.merge(e));
            return true;
        }).orElse(false);
    }
    //</editor-fold>
}
