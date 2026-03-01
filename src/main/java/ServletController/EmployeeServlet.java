package ServletController;
//<editor-fold desc="Imports">
import DTO.EmployeeDto;
import Entity.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.EmployeeService;
import util.JpaUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
//</editor-fold>

@WebServlet(urlPatterns = {"/api/v3/employees/*", "/api/v3/employee/*"}, loadOnStartup = 1)
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="Constants">
    private static final String JSON = "application/json; charset=UTF-8";
    //</editor-fold>

    //<editor-fold desc="Fields">
    private EmployeeService service;
    //</editor-fold>

    //<editor-fold desc="Lifecycle">
    @Override
    public void init() throws ServletException {
        try {
            service = new EmployeeService(JpaUtil.getEntityManagerFactory());
        } catch (Exception e) {
            throw new ServletException("JPA not available", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="GET - Read">
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            int offset = getIntParam(req, "offset", 0);
            int limit = getIntParam(req, "limit", 5);
            if (limit < 1) limit = 1;
            if (limit > 20) limit = 20;
            List<EmployeeDto> list = service.findAll(offset, limit);
            resp.setStatus(200);
            resp.getWriter().write(JsonUtil.toJson(list));
            return;
        }
        Optional<Long> id = parseId(path);
        if (id.isEmpty()) {
            sendError(resp, 400, "Invalid ID");
            return;
        }
        Optional<EmployeeDto> one = service.findById(id.get());
        if (one.isEmpty()) {
            sendError(resp, 404, "Employee not found");
            return;
        }
        resp.setStatus(200);
        resp.getWriter().write(JsonUtil.toJson(one.get()));
    }
    //</editor-fold>

    //<editor-fold desc="POST - Create">
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        if (req.getPathInfo() != null && !req.getPathInfo().equals("/")) {
            sendError(resp, 400, "POST to /employees only");
            return;
        }
        Employee body = JsonUtil.fromJson(req.getReader(), Employee.class);
        if (body == null) {
            sendError(resp, 400, "Invalid JSON");
            return;
        }
        try {
            Employee created = service.create(body);
            resp.setStatus(201);
            resp.setHeader("Location", location(req, created.getId()));
        } catch (Exception e) {
            sendError(resp, 500, "Failed to create");
        }
    }
    //</editor-fold>

    //<editor-fold desc="PUT - Update">
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        Optional<Long> id = parseId(req.getPathInfo());
        if (id.isEmpty()) {
            sendError(resp, 400, "ID required");
            return;
        }
        Employee body = JsonUtil.fromJson(req.getReader(), Employee.class);
        if (body == null) {
            sendError(resp, 400, "Invalid JSON");
            return;
        }
        Optional<Employee> updated = service.update(id.get(), body);
        if (updated.isEmpty()) {
            sendError(resp, 404, "Employee not found");
            return;
        }
        resp.setStatus(204);
        resp.setHeader("Location", location(req, id.get()));
    }
    //</editor-fold>

    //<editor-fold desc="DELETE">
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON);
        Optional<Long> id = parseId(req.getPathInfo());
        if (id.isEmpty()) {
            sendError(resp, 400, "ID required");
            return;
        }
        if (!service.deleteById(id.get())) {
            sendError(resp, 404, "Employee not found");
            return;
        }
        resp.setStatus(204);
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private static int getIntParam(HttpServletRequest req, String name, int defaultValue) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Optional<Long> parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return Optional.empty();
        }
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        resp.getWriter().write("{\"message\":\"" + escaped + "\"}");
    }

    private static String location(HttpServletRequest req, long id) {
        String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
        return base + "/api/v3/employee/" + id;
    }
    //</editor-fold>
}
