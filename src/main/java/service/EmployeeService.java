package service;
//<editor-fold desc="Imports">
import DAO.GenericDao;
import DTO.EmployeeDto;
import DTO.HobbyDto;
import Entity.Employee;
import Entity.Hobby;
import exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//</editor-fold>

public class EmployeeService {

    //<editor-fold desc="Fields">
    private static final Logger LOG = LogManager.getLogger(EmployeeService.class);
    private final EntityManagerFactory emf;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EmployeeService(EntityManagerFactory emf) {
        this.emf = emf;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    /** Get all employees (summary, no hobbies). */
    public List<EmployeeDto> findAll(int offset, int limit) {
        LOG.debug("findAll offset={}, limit={}", offset, limit);
        EntityManager em = emf.createEntityManager();
        try {
            GenericDao<Employee, Long> dao = new GenericDao<>(em, Employee.class, Employee::getId);
            List<Employee> employees = dao.findAll(offset, limit);
            List<EmployeeDto> result = new ArrayList<>();
            for (Employee e : employees) {
                result.add(toSummaryDto(e));
            }
            LOG.info("Found {} employees", result.size());
            return result;
        } finally {
            em.close();
        }
    }

    /** Get one employee by id (with hobbies). */
    public Optional<EmployeeDto> findById(Long id) {
        LOG.debug("findById id={}", id);
        EntityManager em = emf.createEntityManager();
        try {
            Optional<Employee> employee = findEmployeeWithHobbies(em, id);
            return employee.map(this::toDetailsDto);
        } finally {
            em.close();
        }
    }

    /** Get one employee by id or throw ResourceNotFoundException. */
    public EmployeeDto getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> {
            LOG.warn("Employee not found: id={}", id);
            return new ResourceNotFoundException("Employee not found: " + id);
        });
    }

    /** Create employee. */
    public Employee create(Employee employee) {
        LOG.info("Creating employee: name={}", employee != null ? employee.getName() : null);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            normalize(employee);
            linkHobbiesTo(employee, employee);
            GenericDao<Employee, Long> dao = new GenericDao<>(em, Employee.class, Employee::getId);
            Employee saved = dao.save(employee);
            tx.commit();
            LOG.info("Created employee id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            LOG.error("Failed to create employee", e);
            throw e;
        } finally {
            em.close();
        }
    }

    /** Update employee by id. */
    public Optional<Employee> update(Long id, Employee employee) {
        LOG.debug("Updating employee id={}", id);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            GenericDao<Employee, Long> empDao = new GenericDao<>(em, Employee.class, Employee::getId);
            GenericDao<Hobby, Long> hobbyDao = new GenericDao<>(em, Hobby.class, Hobby::getId);
            Employee managed = empDao.findById(id).orElse(null);
            if (managed == null) {
                LOG.warn("Update failed: employee not found id={}", id);
                tx.rollback();
                return Optional.empty();
            }
            normalize(employee);
            managed.setName(employee.getName());
            managed.setGender(employee.getGender());
            managed.setDateOfBirth(employee.getDateOfBirth());
            managed.setPhoneNumber(employee.getPhoneNumber());
            managed.getHobbies().clear();
            if (employee.getHobbies() != null) {
                for (Hobby h : employee.getHobbies()) {
                    if (h != null) {
                        h.setEmployee(managed);
                        hobbyDao.save(h);
                        managed.getHobbies().add(h);
                    }
                }
            }
            empDao.update(managed);
            tx.commit();
            LOG.info("Updated employee id={}", id);
            return Optional.of(managed);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            LOG.error("Failed to update employee id={}", id, e);
            throw e;
        } finally {
            em.close();
        }
    }

    /** Delete employee by id. */
    public boolean deleteById(Long id) {
        LOG.debug("Deleting employee id={}", id);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            GenericDao<Employee, Long> dao = new GenericDao<>(em, Employee.class, Employee::getId);
            boolean removed = dao.deleteById(id);
            tx.commit();
            if (removed) {
                LOG.info("Deleted employee id={}", id);
            } else {
                LOG.warn("Delete: employee not found id={}", id);
            }
            return removed;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            LOG.error("Failed to delete employee id={}", id, e);
            throw e;
        } finally {
            em.close();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private Optional<Employee> findEmployeeWithHobbies(EntityManager em, Long id) {
        if (id == null) return Optional.empty();
        List<Employee> list = em.createQuery(
            "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.hobbies WHERE e.id = :id", Employee.class)
            .setParameter("id", id).getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private void normalize(Employee e) {
        if (e == null) return;
        if (e.getName() == null) e.setName("");
        if (e.getGender() == null) e.setGender("");
        if (e.getDateOfBirth() == null) e.setDateOfBirth("");
        if (e.getPhoneNumber() == null) e.setPhoneNumber("");
    }

    private void linkHobbiesTo(Employee source, Employee target) {
        if (source.getHobbies() == null) return;
        for (Hobby h : source.getHobbies()) {
            if (h != null) h.setEmployee(target);
        }
    }

    private EmployeeDto toSummaryDto(Employee e) {
        return e == null ? null : new EmployeeDto(e.getId(), e.getName(), e.getGender(), e.getDateOfBirth(), e.getPhoneNumber(), null);
    }

    private EmployeeDto toDetailsDto(Employee e) {
        if (e == null) return null;
        List<HobbyDto> hobbies = e.getHobbies() == null ? List.of() : e.getHobbies().stream()
            .filter(h -> h != null)
            .map(h -> new HobbyDto(h.getId(), h.getName()))
            .collect(Collectors.toList());
        return new EmployeeDto(e.getId(), e.getName(), e.getGender(), e.getDateOfBirth(), e.getPhoneNumber(), hobbies);
    }
    //</editor-fold>
}
