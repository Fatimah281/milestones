package servlet;
//<editor-fold desc="Imports">
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Employee;
import service.EmployeeService;
import util.JsonUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//</editor-fold>

@WebServlet(urlPatterns = {"/api/v2/employees/*", "/api/v2/employee/*" })
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="Constants">
    private static final Logger LOG = Logger.getLogger(EmployeeServlet.class.getName());
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";
    //</editor-fold>

    //<editor-fold desc="Fields">
    private EmployeeService employeeService;
    //</editor-fold>

    //<editor-fold desc="Lifecycle">
    @Override
    public void init() throws ServletException {
        try {
            employeeService = new EmployeeService();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start EmployeeService (check DataSource)", e);
            throw new ServletException("DataSource not available", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="GET - Read Employee(s)">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<Employee> employees = null;
            try {
                employees = employeeService.findAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employees));
            return;
        }

        Integer id = parseIdFromPath(pathInfo);
        if (id == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
            return;
        }

        try {
            Employee employee = employeeService.findById(id);
            if (employee == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employee));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "GET failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="POST - Create Employee">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "POST only to /employees");
            return;
        }

        try {
            Employee employee = JsonUtil.fromJson(request.getReader(), Employee.class);
            if (employee == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
                return;
            }
            Employee saved = employeeService.saveEmployeeWithHobbies(employee);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setHeader("Location", buildEmployeeUrl(request, saved.getId()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "POST failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save employee");
        }
    }
    //</editor-fold>

    //<editor-fold desc="PUT - Update Employee">
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);

        Integer id = parseIdFromPath(request.getPathInfo());
        if (id == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required in path (e.g. /5)");
            return;
        }

        try {
            Employee employee = JsonUtil.fromJson(request.getReader(), Employee.class);
            if (employee == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
                return;
            }
            employee.setId(id);
            int updated = employeeService.update(employee);
            if (updated == 0) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.setHeader("Location", buildEmployeeUrl(request, id));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "PUT failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="DELETE - Delete Employee">
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);

        Integer id = parseIdFromPath(request.getPathInfo());
        if (id == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required in path (e.g. /5)");
            return;
        }

        try {
            int deleted = employeeService.delete(id);
            if (deleted == 0) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DELETE failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    /** Set response type to JSON so the client knows how to read the body. */
    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);
    }

    private Integer parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Send a JSON error message and set status code. */
    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        String safeMessage = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + safeMessage + "\"}");
    }

    /** Build full URL for an employee, e.g. http://localhost:8080/servlet_employee/api/v2/employee/5 */
    private String buildEmployeeUrl(HttpServletRequest request, int id) {
        return request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort()
                + request.getContextPath() + "/api/v2/employee/" + id;
    }
    //</editor-fold>
}
