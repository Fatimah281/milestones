package service;
//<editor-fold desc="Imports">
import DTO.EmployeeDto;
import DTO.HobbyDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Employee;
import model.Hobby;
import repository.EmployeeRepository;
import repository.HobbyRepository;
import util.JpaUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public Optional<EmployeeDto> findByIdDto(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            EmployeeRepository repo = new EmployeeRepository(em);
            return repo.findByIdWithHobbies(id).map(EmployeeService::toDetailsDto);
        } finally {
            em.close();
        }
    }

    public List<EmployeeDto> findAllSummaries(int offset, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            EmployeeRepository repo = new EmployeeRepository(em);
            return repo.findAll(offset, limit).stream()
                .map(EmployeeService::toSummaryDto)
                .collect(Collectors.toList());
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
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static void normalizeEmployee(Employee employee) {
        if (employee == null) return;
        employee.setName(employee.getName() != null ? employee.getName() : "");
        employee.setGender(employee.getGender() != null ? employee.getGender() : "");
        employee.setDateOfBirth(employee.getDateOfBirth() != null ? employee.getDateOfBirth() : "");
        employee.setPhoneNumber(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");
    }

    private static EmployeeDto toSummaryDto(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new EmployeeDto(
            employee.getId(),
            employee.getName(),
            employee.getGender(),
            employee.getDateOfBirth(),
            employee.getPhoneNumber(),
            null
        );
    }

    private static EmployeeDto toDetailsDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        List<HobbyDto> hobbies = employee.getHobbies() == null
            ? List.of()
            : employee.getHobbies().stream()
                .filter(h -> h != null)
                .map(h -> new HobbyDto(h.getId(), h.getName()))
                .collect(Collectors.toList());

        return new EmployeeDto(
            employee.getId(),
            employee.getName(),
            employee.getGender(),
            employee.getDateOfBirth(),
            employee.getPhoneNumber(),
            hobbies
        );
    }
    //</editor-fold>
}
