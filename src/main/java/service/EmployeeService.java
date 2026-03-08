package service;
//<editor-fold desc="Imports">
import DAO.GenericDao;
import DTO.EmployeeDto;
import DTO.HobbyDto;
import DTO.PagedResult;
import Entity.Employee;
import Entity.Hobby;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import exception.ValidationException;
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
    public PagedResult findAll(int offset, int limit) {
        LOG.debug("findAll offset={}, limit={}", offset, limit);
        EntityManager em = emf.createEntityManager();
        try {
            long totalCount = countEmployeesFromDb(em);
            GenericDao<Employee, Long> dao = new GenericDao<>(em, Employee.class, Employee::getId);
            List<Employee> employees = dao.findAll(offset, limit);
            if (employees.isEmpty()) {
                return new PagedResult(totalCount, offset, limit, List.of());
            }
            List<Long> ids = employees.stream().map(Employee::getId).collect(Collectors.toList());
            List<Hobby> hobbiesList = em.createQuery(
                    "SELECT h FROM Hobby h WHERE h.employee.id IN :ids", Hobby.class)
                    .setParameter("ids", ids)
                    .getResultList();
            Map<Long, List<Hobby>> hobbiesByEmployeeId = hobbiesList.stream()
                    .collect(Collectors.groupingBy(h -> h.getEmployee().getId()));
            List<EmployeeDto> data = new ArrayList<>();
            for (Employee e : employees) {
                List<Hobby> empHobbies = hobbiesByEmployeeId.getOrDefault(e.getId(), List.of());
                data.add(toDetailsDto(e, empHobbies));
            }
            LOG.info("Found {} of {} employees (offset={}, limit={})", data.size(), totalCount, offset, limit);
            return new PagedResult(totalCount, offset, limit, data);
        } finally {
            em.close();
        }
    }

    public Optional<EmployeeDto> findById(Long id) {
        LOG.debug("findById id={}", id);
        EntityManager em = emf.createEntityManager();
        try {
            GenericDao<Employee, Long> dao = new GenericDao<>(em, Employee.class, Employee::getId);
            Optional<Employee> employee = dao.findById(id);
            if (employee.isEmpty()) {
                return Optional.empty();
            }
            List<Hobby> empHobbies = em.createQuery(
                    "SELECT h FROM Hobby h WHERE h.employee.id = :id", Hobby.class)
                    .setParameter("id", id)
                    .getResultList();
            return Optional.of(toDetailsDto(employee.get(), empHobbies));
        } finally {
            em.close();
        }
    }

    public Employee create(Employee employee) {
        LOG.info("Creating employee: name={}", employee != null ? employee.getName() : null);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            normalize(employee);
            deduplicateHobbies(employee);
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
                LOG.error("Update failed: employee not found id={}", id);
                tx.rollback();
                return Optional.empty();
            }
            normalize(employee);
            deduplicateHobbies(employee);
            validateHobbiesNotAlreadyPresent(managed, employee.getHobbies());
            managed.setName(employee.getName());
            managed.setGender(employee.getGender());
            managed.setDateOfBirth(employee.getDateOfBirth());
            managed.setPhoneNumber(employee.getPhoneNumber());
            mergeHobbiesFromRequest(managed, employee.getHobbies(), em, hobbyDao);
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
                LOG.error("Delete: employee not found id={}", id);
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
    private long countEmployeesFromDb(EntityManager em) {
        Query q = em.createNativeQuery("SELECT COUNT(*) FROM EMP_HOB.EMPLOYEE");
        Number n = (Number) q.getSingleResult();
        long count = n == null ? 0L : n.longValue();
        LOG.trace("DB row count for EMPLOYEE: {}", count);
        return count;
    }

    private void normalize(Employee e) {
        if (e == null) return;
        if (e.getName() == null) e.setName("");
        if (e.getGender() == null) e.setGender("");
        if (e.getDateOfBirth() == null) e.setDateOfBirth("");
        if (e.getPhoneNumber() == null) e.setPhoneNumber("");
    }

    private void deduplicateHobbies(Employee e) {
        if (e == null || e.getHobbies() == null) return;
        List<Hobby> list = e.getHobbies();
        Set<String> seen = new LinkedHashSet<>();
        list.removeIf(h -> {
            if (h == null || h.getName() == null) return true;
            if (!seen.add(h.getName().trim().toLowerCase())) return true;
            return false;
        });
    }

    private void applyHobbiesFromRequest(Employee managed, List<Hobby> requested, EntityManager em, GenericDao<Hobby, Long> hobbyDao) {
        List<Hobby> existing = new ArrayList<>(managed.getHobbies());
        Map<String, Hobby> existingByName = new LinkedHashMap<>();
        for (Hobby h : existing) {
            if (h != null && h.getName() != null) {
                existingByName.putIfAbsent(h.getName().trim().toLowerCase(), h);
            }
        }
        Set<String> requestedNames = new LinkedHashSet<>();
        if (requested != null) {
            for (Hobby h : requested) {
                if (h != null && h.getName() != null && !h.getName().isBlank()) {
                    requestedNames.add(h.getName().trim().toLowerCase());
                }
            }
        }
        for (Hobby h : existing) {
            if (h == null || h.getName() == null) continue;
            if (!requestedNames.contains(h.getName().trim().toLowerCase())) {
                em.remove(em.contains(h) ? h : em.merge(h));
            }
        }
        managed.getHobbies().clear();
        if (requested == null) return;
        for (Hobby h : requested) {
            if (h == null || h.getName() == null || h.getName().isBlank()) continue;
            String key = h.getName().trim().toLowerCase();
            Hobby toAdd = existingByName.get(key);
            if (toAdd != null) {
                managed.getHobbies().add(toAdd);
            } else {
                h.setEmployee(managed);
                hobbyDao.save(h);
                managed.getHobbies().add(h);
            }
        }
    }

    private void validateHobbiesNotAlreadyPresent(Employee managed, List<Hobby> requested) {
        if (managed.getHobbies() == null || requested == null) return;
        Set<String> existingNames = new LinkedHashSet<>();
        for (Hobby h : managed.getHobbies()) {
            if (h != null && h.getName() != null && !h.getName().isBlank()) {
                existingNames.add(h.getName().trim().toLowerCase());
            }
        }
        Set<String> reportedKeys = new LinkedHashSet<>();
        List<String> alreadyPresent = new ArrayList<>();
        for (Hobby h : requested) {
            if (h == null || h.getName() == null || h.getName().isBlank()) continue;
            String key = h.getName().trim().toLowerCase();
            if (existingNames.contains(key) && reportedKeys.add(key)) {
                alreadyPresent.add(h.getName().trim());
            }
        }
        if (!alreadyPresent.isEmpty()) {
            LOG.warn("PUT rejected: hobbies already exist for employee: {}", alreadyPresent);
            throw new ValidationException(
                    "Hobby already exists for this employee. Do not add a value that already exists: " + String.join(", ", alreadyPresent),
                    List.of("hobbies: " + String.join(", ", alreadyPresent) + " already exist for this employee"));
        }
    }

    private void mergeHobbiesFromRequest(Employee managed, List<Hobby> requested, EntityManager em, GenericDao<Hobby, Long> hobbyDao) {
        if (managed.getHobbies() == null) return;
        Map<String, Hobby> existingByName = new LinkedHashMap<>();
        for (Hobby h : managed.getHobbies()) {
            if (h != null && h.getName() != null) {
                existingByName.putIfAbsent(h.getName().trim().toLowerCase(), h);
            }
        }
        if (requested == null) return;
        for (Hobby h : requested) {
            if (h == null || h.getName() == null || h.getName().isBlank()) continue;
            String key = h.getName().trim().toLowerCase();
            if (existingByName.containsKey(key)) continue;
            h.setEmployee(managed);
            hobbyDao.save(h);
            managed.getHobbies().add(h);
            existingByName.put(key, h);
        }
    }

    private void linkHobbiesTo(Employee source, Employee target) {
        if (source.getHobbies() == null) return;
        for (Hobby h : source.getHobbies()) {
            if (h != null) h.setEmployee(target);
        }
    }

    private EmployeeDto toDetailsDto(Employee e, List<Hobby> hobbies) {
        if (e == null) return null;
        List<HobbyDto> hobbyList = new ArrayList<>();
        if (hobbies != null) {
            for (Hobby h : hobbies) {
                if (h != null) {
                    hobbyList.add(new HobbyDto(h.getId(), h.getName()));
                }
            }
        }
        return new EmployeeDto(e.getId(), e.getName(), e.getGender(), e.getDateOfBirth(), e.getPhoneNumber(), hobbyList);
    }
    //</editor-fold>
}
