package service;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Employee;
import model.Hobby;
import repository.EmployeeRepository;
import repository.HobbyRepository;
import util.JpaUtil;

import java.util.List;
import java.util.Optional;
//</editor-fold>

public class EmployeeService {

    //<editor-fold desc="Fields">
    private final jakarta.persistence.EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public Employee save(Employee employee) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            normalizeEmployee(employee);
            if (employee.getHobbies() != null) {
                for (Hobby h : employee.getHobbies()) {
                    if (h != null) {
                        h.setEmployee(employee);
                    }
                }
            }
            Employee saved = new EmployeeRepository(em).save(employee);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Employee> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            EmployeeRepository repo = new EmployeeRepository(em);
            return repo.findById(id);
        } finally {
            em.close();
        }
    }

    public List<Employee> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            EmployeeRepository repo = new EmployeeRepository(em);
            return repo.findAll();
        } finally {
            em.close();
        }
    }

    public Employee update(Employee employee) {
        if (employee.getId() == null) {
            throw new IllegalArgumentException("Employee id is required for update.");
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            normalizeEmployee(employee);
            EmployeeRepository employeeRepo = new EmployeeRepository(em);
            HobbyRepository hobbyRepo = new HobbyRepository(em);
            Employee managed = employeeRepo.findById(employee.getId()).orElse(null);
            if (managed == null) {
                tx.rollback();
                return null;
            }
            managed.setName(employee.getName());
            managed.setGender(employee.getGender());
            managed.setDateOfBirth(employee.getDateOfBirth());
            managed.setPhoneNumber(employee.getPhoneNumber());
            managed.getHobbies().clear();
            if (employee.getHobbies() != null) {
                for (Hobby h : employee.getHobbies()) {
                    if (h != null) {
                        h.setEmployee(managed);
                        hobbyRepo.save(h);
                        managed.getHobbies().add(h);
                    }
                }
            }
            employeeRepo.update(managed);
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean delete(Employee employee) {
        if (employee == null || employee.getId() == null) {
            return false;
        }
        return deleteById(employee.getId());
    }

    public boolean deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EmployeeRepository repo = new EmployeeRepository(em);
            boolean removed = repo.deleteById(id);
            tx.commit();
            return removed;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void assignHobby(Employee employee, Hobby hobby) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EmployeeRepository employeeRepo = new EmployeeRepository(em);
            HobbyRepository hobbyRepo = new HobbyRepository(em);
            Employee managedEmp = employeeRepo.findById(employee.getId()).orElseThrow();
            Hobby managedHobby = hobby.getId() != null
                    ? hobbyRepo.findById(hobby.getId()).orElse(hobby)
                    : hobby;
            if (managedHobby.getId() == null) {
                managedHobby.setEmployee(managedEmp);
                hobbyRepo.save(managedHobby);
            }
            employeeRepo.assignHobby(managedEmp, managedHobby);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void removeHobby(Employee employee, Hobby hobby) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EmployeeRepository employeeRepo = new EmployeeRepository(em);
            HobbyRepository hobbyRepo = new HobbyRepository(em);
            Employee managedEmp = employeeRepo.findById(employee.getId()).orElseThrow();
            Hobby managedHobby = hobbyRepo.findById(hobby.getId()).orElseThrow();
            employeeRepo.removeHobby(managedEmp, managedHobby);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static void normalizeEmployee(Employee employee) {
        if (employee == null) return;
        employee.setName(employee.getName() != null ? employee.getName() : "");
        employee.setGender(employee.getGender() != null ? employee.getGender() : "");
        employee.setDateOfBirth(employee.getDateOfBirth() != null ? employee.getDateOfBirth() : "");
        employee.setPhoneNumber(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");
    }
    //</editor-fold>
}
