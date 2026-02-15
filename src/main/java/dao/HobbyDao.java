package dao;
//<editor-fold desc="Imports">
import model.Hobby;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
//</editor-fold>

/**
 * DAO for Hobby table.
 */
public class HobbyDao {

    //<editor-fold desc="SQL Constants">
    private static final String SCHEMA = "EMP_HOB";
    private static final String INSERT = "INSERT INTO " + SCHEMA + ".HOBBY (EMPLOYEE_ID, NAME) VALUES (?, ?)";
    private static final String SELECT_BY_EMPLOYEE_ID =
            "SELECT ID, EMPLOYEE_ID, NAME FROM " + SCHEMA + ".HOBBY WHERE EMPLOYEE_ID = ? ORDER BY ID";
    private static final String DELETE_BY_EMPLOYEE_ID = "DELETE FROM " + SCHEMA + ".HOBBY WHERE EMPLOYEE_ID = ?";
    //</editor-fold>

    //<editor-fold desc="Fields">
    private final DataSource dataSource;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public HobbyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    /**
     * Inserts all hobbies for an employee and sets their ids and employeeId on the entities.
     */
    public void insertAll(Connection conn, int employeeId, List<Hobby> hobbies) throws SQLException {
        if (hobbies == null || hobbies.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            for (Hobby h : hobbies) {
                ps.setInt(1, employeeId);
                ps.setString(2, h.getName() != null ? h.getName() : "");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        h.setId(rs.getInt(1));
                        h.setEmployeeId(employeeId);
                    }
                }
            }
        }
    }

    public List<Hobby> findByEmployeeId(Connection conn, int employeeId) throws SQLException {
        List<Hobby> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int deleteByEmployeeId(Connection conn, int employeeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_EMPLOYEE_ID)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static Hobby mapRow(ResultSet rs) throws SQLException {
        Hobby h = new Hobby();
        h.setId(rs.getInt("ID"));
        h.setEmployeeId(rs.getInt("EMPLOYEE_ID"));
        h.setName(rs.getString("NAME"));
        return h;
    }
    //</editor-fold>
}
