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
        dataSource = DataSourceProvider.getDataSource();
        employeeDao = new EmployeeDao(dataSource);
        hobbyDao = new HobbyDao(dataSource);
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public Employee saveEmployeeWithHobbies(Employee employee) throws SQLException {
        employee.setId(null);
        fillEmptyFields(employee);
        fillEmptyHobbyNames(employee.getHobbies());

        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            int newId = employeeDao.insert(conn, employee);
            employee.setId(newId);
            List<Hobby> hobbies = employee.getHobbies();
            if (hobbies != null && !hobbies.isEmpty()) {
                hobbyDao.insertAll(conn, newId, hobbies);
            }
            conn.commit();
            return employee;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            closeConnection(conn);
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
            for (Employee emp : employees) {
                List<Hobby> hobbies = hobbyDao.findByEmployeeId(conn, emp.getId());
                emp.setHobbies(hobbies);
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
            int rows = employeeDao.update(conn, employee);
            hobbyDao.deleteByEmployeeId(conn, employee.getId());
            if (employee.getHobbies() != null && !employee.getHobbies().isEmpty()) {
                hobbyDao.insertAll(conn, employee.getId(), employee.getHobbies());
            }
            conn.commit();
            return rows;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            closeConnection(conn);
        }
    }

    /** Delete employee and their hobbies. Returns number of rows deleted (0 or 1). */
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
            closeConnection(conn);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private void fillEmptyFields(Employee employee) {
        if (employee == null) return;
        if (employee.getName() == null) employee.setName("");
        if (employee.getGender() == null) employee.setGender("");
        if (employee.getDateOfBirth() == null) employee.setDateOfBirth("");
        if (employee.getPhoneNumber() == null) employee.setPhoneNumber("");
    }

    /** Replace null hobby names with empty string. */
    private void fillEmptyHobbyNames(List<Hobby> hobbies) {
        if (hobbies == null) return;
        for (Hobby h : hobbies) {
            if (h != null && h.getName() == null) {
                h.setName("");
            }
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    /** Reset autoCommit and close the connection. */
    private void closeConnection(Connection conn) {
        if (conn == null) return;
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }
    //</editor-fold>
}
