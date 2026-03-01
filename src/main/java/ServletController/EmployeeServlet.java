package ServletController;
//<editor-fold desc="Imports">
import DTO.ApiError;
import Entity.Employee;
import exception.InvalidJsonException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.EmployeeService;
import util.JpaUtil;
import util.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
//</editor-fold>

@WebServlet(urlPatterns = {"/api/v4/employees/*", "/api/v4/employee/*"}, loadOnStartup = 1)
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="Constants">
    private static final String JSON = "application/json; charset=UTF-8";
    private static final Logger LOG = LogManager.getLogger(EmployeeServlet.class);
    //</editor-fold>

    //<editor-fold desc="Fields">
    private EmployeeService service;
    //</editor-fold>

    //<editor-fold desc="Lifecycle">
    @Override
    public void init() throws ServletException {
        LOG.trace("Servlet init started");
        try {
            service = new EmployeeService(JpaUtil.getEntityManagerFactory());
            LOG.info("EmployeeServlet initialized successfully");
        } catch (Exception e) {
            LOG.fatal("JPA not available - servlet cannot start", e);
            throw new ServletException("JPA not available", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="GET - Read">
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        LOG.debug("GET {}", req.getRequestURI());
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                int offset = param(req, "offset", 0);
                int limit = Math.max(1, Math.min(20, param(req, "limit", 5)));
                LOG.trace("List employees offset={}, limit={}", offset, limit);
                List<?> list = service.findAll(offset, limit);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JsonUtil.toJson(list));
                return;
            }
            Optional<Long> id = parseId(path);
            if (id.isEmpty()) {
                LOG.warn("GET invalid path: {}", path);
                sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid ID");
                return;
            }
            Optional<?> one = service.findById(id.get());
            if (one.isEmpty()) {
                LOG.warn("GET employee not found: id={}", id.get());
                sendApiError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JsonUtil.toJson(one.get()));
        } catch (Exception e) {
            handleError(resp, "GET", req.getRequestURI(), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="POST - Create">
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        LOG.debug("POST {}", req.getRequestURI());
        try {
            if (req.getPathInfo() != null && !req.getPathInfo().equals("/")) {
                LOG.warn("POST to non-root path: {}", req.getPathInfo());
                sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "POST to /employees only");
                return;
            }
            Employee body = JsonUtil.fromJsonWithEmployeeValidation(req.getReader(), Employee.class);
            Employee created = service.create(body);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Location", location(req, created.getId()));
            LOG.info("Created employee id={}", created.getId());
        } catch (ValidationException e) {
            LOG.warn("Validation failed on POST: {}", e.getMessage());
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Validation Failed", e.getMessage(), e.getDetails());
        } catch (InvalidJsonException e) {
            LOG.warn("Invalid JSON on POST: {}", e.getMessage());
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", e.getMessage());
        } catch (Exception e) {
            handleError(resp, "POST", req.getRequestURI(), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="PUT - Update">
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        LOG.debug("PUT {}", req.getRequestURI());
        try {
            Optional<Long> id = parseId(req.getPathInfo());
            if (id.isEmpty()) {
                sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "ID required");
                return;
            }
            Employee body = JsonUtil.fromJsonWithEmployeeValidation(req.getReader(), Employee.class);
            Optional<Employee> updated = service.update(id.get(), body);
            if (updated.isEmpty()) {
                sendApiError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.setHeader("Location", location(req, id.get()));
            LOG.info("Updated employee id={}", id.get());
        } catch (ValidationException e) {
            LOG.warn("Validation failed on PUT: {}", e.getMessage());
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Validation Failed", e.getMessage(), e.getDetails());
        } catch (InvalidJsonException e) {
            LOG.warn("Invalid JSON on PUT: {}", e.getMessage());
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", e.getMessage());
        } catch (Exception e) {
            handleError(resp, "PUT", req.getRequestURI(), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="DELETE">
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        LOG.debug("DELETE {}", req.getRequestURI());
        try {
            Optional<Long> id = parseId(req.getPathInfo());
            if (id.isEmpty()) {
                sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "ID required");
                return;
            }
            if (!service.deleteById(id.get())) {
                sendApiError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found", "Employee not found");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            LOG.info("Deleted employee id={}", id.get());
        } catch (Exception e) {
            handleError(resp, "DELETE", req.getRequestURI(), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static int param(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Optional<Long> parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) return Optional.empty();
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void sendApiError(HttpServletResponse resp, int status, String error, String message) throws IOException {
        sendApiError(resp, status, error, message, null);
    }

    private void sendApiError(HttpServletResponse resp, int status, String error, String message, List<String> details) throws IOException {
        resp.setStatus(status);
        ApiError apiError = details != null ? new ApiError(error, message, details) : new ApiError(error, message);
        resp.getWriter().write(JsonUtil.toJson(apiError));
    }

    /** Centralized handling for unexpected exceptions. */
    private void handleError(HttpServletResponse resp, String method, String uri, Exception e) throws IOException {
        if (e instanceof ValidationException ve) {
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Validation Failed", ve.getMessage(), ve.getDetails());
            return;
        }
        if (e instanceof InvalidJsonException) {
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", e.getMessage());
            return;
        }
        if (e instanceof ResourceNotFoundException) {
            sendApiError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found", e.getMessage());
            return;
        }
        if (e.getCause() instanceof JsonProcessingException) {
            LOG.warn("Malformed JSON: {}", e.getCause().getMessage());
            sendApiError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "Invalid JSON syntax");
            return;
        }
        LOG.error("{} {} failed", method, uri, e);
        sendApiError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.");
    }

    private static String location(HttpServletRequest req, long id) {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath() + "/api/v3/employee/" + id;
    }
    //</editor-fold>
}
