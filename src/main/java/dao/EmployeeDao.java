package dao;
//<editor-fold desc="Imports">
import model.Employee;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//</editor-fold>

/**
 * DAO for Employee table.
 */
public class EmployeeDao {

    //<editor-fold desc="SQL Constants">
    private static final String SCHEMA = "EMP_HOB";
    private static final String NEXT_ID = "SELECT COALESCE(MAX(ID), 0) + 1 FROM " + SCHEMA + ".EMPLOYEE";
    private static final String INSERT =
            "INSERT INTO " + SCHEMA + ".EMPLOYEE (ID, NAME, GENDER, DATE_OF_BIRTH, PHONE_NUMBER) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID =
            "SELECT ID, NAME, GENDER, DATE_OF_BIRTH, PHONE_NUMBER FROM " + SCHEMA + ".EMPLOYEE WHERE ID = ?";
    private static final String SELECT_ALL =
            "SELECT ID, NAME, GENDER, DATE_OF_BIRTH, PHONE_NUMBER FROM " + SCHEMA + ".EMPLOYEE ORDER BY ID";
    private static final String UPDATE =
            "UPDATE " + SCHEMA + ".EMPLOYEE SET NAME = ?, GENDER = ?, DATE_OF_BIRTH = ?, PHONE_NUMBER = ? WHERE ID = ?";
    private static final String DELETE = "DELETE FROM " + SCHEMA + ".EMPLOYEE WHERE ID = ?";
    //</editor-fold>

    //<editor-fold desc="Fields">
    private final DataSource dataSource;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EmployeeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    /**
     * Inserts an employee and returns the new ID.
     */
    public int insert(Connection conn, Employee employee) throws SQLException {
        int nextId;
        try (PreparedStatement ps = conn.prepareStatement(NEXT_ID);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nextId = rs.getInt(1);
            } else {
                throw new SQLException("Could not get next employee ID.");
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setInt(1, nextId);
            ps.setString(2, nullSafe(employee.getName()));
            ps.setString(3, nullSafe(employee.getGender()));
            ps.setString(4, nullSafe(employee.getDateOfBirth()));
            ps.setString(5, nullSafe(employee.getPhoneNumber()));
            ps.executeUpdate();
        }
        return nextId;
    }

    public Employee findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Employee> findAll(Connection conn) throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int update(Connection conn, Employee employee) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, nullSafe(employee.getName()));
            ps.setString(2, nullSafe(employee.getGender()));
            ps.setString(3, nullSafe(employee.getDateOfBirth()));
            ps.setString(4, nullSafe(employee.getPhoneNumber()));
            ps.setInt(5, employee.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("ID"));
        e.setName(rs.getString("NAME"));
        e.setGender(rs.getString("GENDER"));
        e.setDateOfBirth(rs.getString("DATE_OF_BIRTH"));
        e.setPhoneNumber(rs.getString("PHONE_NUMBER"));
        return e;
    }
    //</editor-fold>
}
