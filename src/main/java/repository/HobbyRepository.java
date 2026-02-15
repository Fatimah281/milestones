package repository;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;
import model.Hobby;

import java.util.List;
import java.util.Optional;
//</editor-fold>
public class HobbyRepository {

    //<editor-fold desc="Fields">
    private final EntityManager em;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public HobbyRepository(EntityManager em) {
        this.em = em;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public Hobby save(Hobby h) {
        if (h.getId() == null) {
            em.persist(h);
            return h;
        }
        return em.merge(h);
    }

    public Optional<Hobby> findById(Long id) {
        Hobby h = em.find(Hobby.class, id);
        return Optional.ofNullable(h);
    }

    @SuppressWarnings("unchecked")
    public List<Hobby> findAll() {
        return em.createQuery("SELECT h FROM Hobby h ORDER BY h.id", Hobby.class).getResultList();
    }

    public Hobby update(Hobby h) {
        return em.merge(h);
    }

    public void delete(Hobby h) {
        em.remove(em.contains(h) ? h : em.merge(h));
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
    public Optional<Hobby> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        List<Hobby> list = em.createQuery("SELECT h FROM Hobby h WHERE h.name = :name", Hobby.class)
                .setParameter("name", name.trim())
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
    //</editor-fold>
}
