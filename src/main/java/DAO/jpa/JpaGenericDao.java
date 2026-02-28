package dao.jpa;

import dao.IGenericDao;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JpaGenericDao<T, ID> implements IGenericDao<T, ID> {

  protected final EntityManager em;
  private final Class<T> entityClass;
  private final Function<T, Object> idGetter;

  public JpaGenericDao(EntityManager em, Class<T> entityClass, Function<T, Object> idGetter) {
    this.em = em;
    this.entityClass = entityClass;
    this.idGetter = idGetter;
  }

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
  public void delete(T entity) {
    em.remove(em.contains(entity) ? entity : em.merge(entity));
  }

  @Override
  public boolean deleteById(ID id) {
    return findById(id).map(e -> { delete(e); return true; }).orElse(false);
  }
}
