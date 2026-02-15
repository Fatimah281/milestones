package servlet;
//<editor-fold desc="Imports">
import dto.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.EmployeeService;
import util.JsonSchemaValidator;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Set;
//</editor-fold>

@WebServlet(urlPatterns = {"/api/v3/employees/*", "/api/v3/employee/*"})
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="Constants">
    private static final Logger LOG = LogManager.getLogger(EmployeeServlet.class);
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
            LOG.info("EmployeeServlet initialized");
        } catch (Exception e) {
            LOG.error("Failed to initialize EmployeeService (JPA/EntityManagerFactory)", e);
            throw new ServletException("JPA not available", e);
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
                LOG.info("GET /employees: returned {} item(s)", employees.size());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(JsonUtil.toJson(employees));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid path format");
                return;
            }

            long id = Long.parseLong(parts[1]);
            Employee employee = employeeService.findById(id).orElse(null);
            if (employee == null) {
                LOG.warn("GET /employees/{}: employee not found", id);
                sendApiError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            LOG.info("GET /employees/{}: found employee name={}", id, employee.getName());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employee));
        } catch (NumberFormatException e) {
            LOG.warn("GET: invalid ID format in path");
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid ID format");
        } catch (Exception e) {
            LOG.error("GET failed", e);
            sendApiError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "Server error");
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
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "POST to collection only: /employees");
            return;
        }

        try {
            String body = JsonUtil.readBodyAsString(request.getReader());
            Set<String> validationErrors = JsonSchemaValidator.validateEmployee(body);
            if (!validationErrors.isEmpty()) {
                String message = String.join("; ", validationErrors);
                LOG.warn("POST /employees: validation failed - {}", message);
                sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", message);
                return;
            }
            Employee employee = JsonUtil.fromJson(body, Employee.class);
            Employee saved = employeeService.save(employee);
            LOG.info("POST /employees: created employee id={}, name={}", saved.getId(), saved.getName());
            response.setStatus(HttpServletResponse.SC_CREATED);
            String location = request.getRequestURL().append("/").append(saved.getId()).toString();
            response.setHeader("Location", location);
        } catch (IOException e) {
            LOG.warn("POST /employees: invalid JSON body - {}", e.getMessage());
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid JSON body");
        } catch (Exception e) {
            LOG.error("POST /employees failed", e);
            sendApiError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "Failed to save employee");
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
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Employee ID is required");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid path format");
            return;
        }

        try {
            long id = Long.parseLong(parts[1]);
            String body = JsonUtil.readBodyAsString(request.getReader());
            Set<String> validationErrors = JsonSchemaValidator.validateEmployee(body);
            if (!validationErrors.isEmpty()) {
                String message = String.join("; ", validationErrors);
                LOG.warn("PUT /employees/{}: validation failed - {}", id, message);
                sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", message);
                return;
            }
            Employee employee = JsonUtil.fromJson(body, Employee.class);
            employee.setId(id);
            Employee updated = employeeService.update(employee);
            if (updated == null) {
                LOG.warn("PUT /employees/{}: employee not found", id);
                sendApiError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            LOG.info("PUT /employees/{}: updated employee name={}", id, updated.getName());
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            String location = request.getRequestURL().toString();
            response.setHeader("Location", location);
        } catch (NumberFormatException e) {
            LOG.warn("PUT: invalid ID format in path");
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid ID format");
        } catch (IOException e) {
            LOG.warn("PUT /employees: invalid JSON body - {}", e.getMessage());
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid JSON body");
        } catch (Exception e) {
            LOG.error("PUT failed", e);
            sendApiError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "Server error");
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
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Employee ID is required");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid path format");
            return;
        }

        try {
            long id = Long.parseLong(parts[1]);
            boolean deleted = employeeService.deleteById(id);
            if (!deleted) {
                LOG.warn("DELETE /employees/{}: employee not found", id);
                sendApiError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            LOG.info("DELETE /employees/{}: employee deleted", id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            LOG.warn("DELETE: invalid ID format in path");
            sendApiError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid ID format");
        } catch (Exception e) {
            LOG.error("DELETE failed", e);
            sendApiError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", "Server error");
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    /**
     * Sends a structured JSON error response (centralized exception handling).
     * Clients never see stack traces; full details are logged internally.
     */
    private static void sendApiError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);
        ApiError apiError = new ApiError(error, message);
        response.getWriter().write(JsonUtil.toJson(apiError));
    }
    //</editor-fold>
}
