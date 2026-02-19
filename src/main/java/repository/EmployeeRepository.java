package repository;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;
import model.Employee;

import java.util.List;
import java.util.Optional;
//</editor-fold>
public class EmployeeRepository {

    //<editor-fold desc="Fields">
    private final EntityManager em;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EmployeeRepository(EntityManager em) {
        this.em = em;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public Employee save(Employee e) {
        if (e.getId() == null) {
            em.persist(e);
            return e;
        }
        return em.merge(e);
    }

    public Optional<Employee> findById(Long id) {
        Employee e = em.find(Employee.class, id);
        return Optional.ofNullable(e);
    }

    public List<Employee> findAll(int offset, int limit) {
        return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class)
            .setFirstResult(Math.max(0, offset))
            .setMaxResults(Math.max(1, limit))
            .getResultList();
    }

    public Optional<Employee> findByIdWithHobbies(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        List<Employee> list = em.createQuery(
                "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.hobbies WHERE e.id = :id",
                Employee.class
            )
            .setParameter("id", id)
            .getResultList();

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Employee update(Employee e) {
        return em.merge(e);
    }

    public void delete(Employee e) {
        em.remove(em.contains(e) ? e : em.merge(e));
    }

    public boolean deleteById(Long id) {
        Optional<Employee> existing = findById(id);
        existing.ifPresent(this::delete);
        return existing.isPresent();
    }
    //</editor-fold>
}
