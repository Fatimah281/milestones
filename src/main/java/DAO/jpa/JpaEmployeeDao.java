package dao.jpa;

import dao.IEmployeeDao;
import jakarta.persistence.EntityManager;
import model.Employee;

import java.util.List;
import java.util.Optional;

public class JpaEmployeeDao extends JpaGenericDao<Employee, Long> implements IEmployeeDao {

  public JpaEmployeeDao(EntityManager em) {
    super(em, Employee.class, Employee::getId);
  }

  @Override
  public Optional<Employee> findByIdWithHobbies(Long id) {
    if (id == null) return Optional.empty();
    List<Employee> list = em.createQuery(
        "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.hobbies WHERE e.id = :id",
        Employee.class)
      .setParameter("id", id)
      .getResultList();
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }
}
