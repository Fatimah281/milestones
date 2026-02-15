package repository;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;
import model.Employee;
import model.Hobby;

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

    @SuppressWarnings("unchecked")
    public List<Employee> findAll() {
        return em.createQuery("SELECT e FROM Employee e ORDER BY e.id", Employee.class).getResultList();
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

    public void assignHobby(Employee employee, Hobby hobby) {
        Employee managed = em.merge(employee);
        Hobby managedHobby = em.merge(hobby);
        if (!managed.getHobbies().contains(managedHobby)) {
            managed.getHobbies().add(managedHobby);
            managedHobby.setEmployee(managed);
        }
    }

    /**
     * Removes a hobby from an employee (one-to-many: removes from collection; orphanRemoval deletes the hobby).
     */
    public void removeHobby(Employee employee, Hobby hobby) {
        Employee managed = em.merge(employee);
        Hobby managedHobby = em.merge(hobby);
        managed.getHobbies().remove(managedHobby);
    }
    //</editor-fold>
}
