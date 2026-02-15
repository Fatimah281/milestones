package service;
//<editor-fold desc="Imports">
import dao.EmployeeDao;
import dao.HobbyDao;
import model.Employee;
import model.Hobby;
import util.DataSourceProvider;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
//</editor-fold>

public class EmployeeService {

    //<editor-fold desc="Fields">
    private final DataSource dataSource;
    private final EmployeeDao employeeDao;
    private final HobbyDao hobbyDao;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EmployeeService() throws NamingException {
        this.dataSource = DataSourceProvider.getDataSource();
        this.employeeDao = new EmployeeDao(dataSource);
        this.hobbyDao = new HobbyDao(dataSource);
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public Employee saveEmployeeWithHobbies(Employee employee) throws SQLException {
        employee.setId(null);
        normalizeEmployee(employee);
        normalizeHobbies(employee.getHobbies());
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            int employeeId = employeeDao.insert(conn, employee);
            employee.setId(employeeId);
            List<Hobby> hobbies = employee.getHobbies();
            if (hobbies != null && !hobbies.isEmpty()) {
                hobbyDao.insertAll(conn, employeeId, hobbies);
            }
            conn.commit();
            return employee;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            resetAutoCommitAndClose(conn);
        }
    }

    public Employee findById(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Employee employee = employeeDao.findById(conn, id);
            if (employee != null) {
                List<Hobby> hobbies = hobbyDao.findByEmployeeId(conn, id);
                employee.setHobbies(hobbies);
            }
            return employee;
        }
    }

    public List<Employee> findAll() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            List<Employee> employees = employeeDao.findAll(conn);
            for (Employee e : employees) {
                List<Hobby> hobbies = hobbyDao.findByEmployeeId(conn, e.getId());
                e.setHobbies(hobbies);
            }
            return employees;
        }
    }

    public int update(Employee employee) throws SQLException {
        if (employee.getId() == null) {
            throw new SQLException("Employee id is required for update.");
        }
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            employeeDao.update(conn, employee);
            hobbyDao.deleteByEmployeeId(conn, employee.getId());
            if (employee.getHobbies() != null && !employee.getHobbies().isEmpty()) {
                hobbyDao.insertAll(conn, employee.getId(), employee.getHobbies());
            }
            conn.commit();
            return 1;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            resetAutoCommitAndClose(conn);
        }
    }

    public int delete(int id) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            hobbyDao.deleteByEmployeeId(conn, id);
            int rows = employeeDao.delete(conn, id);
            conn.commit();
            return rows;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            resetAutoCommitAndClose(conn);
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

    private static void normalizeHobbies(List<Hobby> hobbies) {
        if (hobbies == null) return;
        for (Hobby h : hobbies) {
            if (h != null && h.getName() == null) {
                h.setName("");
            }
        }
    }

    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private static void resetAutoCommitAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
    //</editor-fold>
}
