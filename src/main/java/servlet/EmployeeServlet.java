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
            LOG.log(Level.SEVERE, "Failed to initialize EmployeeService (JNDI/DataSource)", e);
            throw new ServletException("DataSource not available", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="GET - Read Employee(s)">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Employee> employees = employeeService.findAll();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(JsonUtil.toJson(employees));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
                return;
            }

            int id = Integer.parseInt(parts[1]);
            Employee employee = employeeService.findById(id);
            if (employee == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employee));
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "GET failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="POST - Create Employee">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "POST to collection only: /employees");
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
            String location = request.getRequestURL().append("/").append(saved.getId()).toString();
            response.setHeader("Location", location);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "POST /employees failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save employee");
        }
    }
    //</editor-fold>

    //<editor-fold desc="PUT - Update Employee">
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
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
            String location = request.getRequestURL().toString();
            response.setHeader("Location", location);
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
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
            int deleted = employeeService.delete(id);
            if (deleted == 0) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DELETE failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    //</editor-fold>
}
