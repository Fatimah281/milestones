package repository;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;
import model.Hobby;
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
    //</editor-fold>
}
