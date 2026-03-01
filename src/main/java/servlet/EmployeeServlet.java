package servlet;
//<editor-fold desc="Imports">

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Employee;
import storage.EmployeeStorage;
import util.JsonUtil;

import java.io.IOException;
import java.util.Collection;
//</editor-fold>

@WebServlet(urlPatterns = {"/api/v1/employees", "/api/v1/employee/*"})
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="Private helpers">
    private static void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    /** Parse employee ID from path like "/1". Returns null if path is invalid. */
    private static Integer parseEmployeeId(String pathInfo) {
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

    private static void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private static void setLocationHeader(HttpServletRequest request, HttpServletResponse response, String path) {
        String location = request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort()
                + request.getContextPath()
                + path;
        response.setHeader("Location", location);
    }
    //</editor-fold>

    //<editor-fold desc="GET - Read Employee(s)">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if ("/api/v1/employees".equals(servletPath)) {
            Collection<Employee> employees = EmployeeStorage.employeeDB.values();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employees));
            return;
        }

        if ("/api/v1/employee".equals(servletPath)) {
            Integer id = parseEmployeeId(pathInfo);
            if (id == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
                return;
            }

            Employee employee = EmployeeStorage.employeeDB.get(id);
            if (employee == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employee));
            return;
        }

        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
    }
    //</editor-fold>

    //<editor-fold desc="POST - Create Employee">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);

        Employee employee = JsonUtil.fromJson(request.getReader(), Employee.class);
        int id = EmployeeStorage.idCounter.getAndIncrement();
        employee.setId(id);
        EmployeeStorage.employeeDB.put(id, employee);

        response.setStatus(HttpServletResponse.SC_CREATED);
        setLocationHeader(request, response, "/api/v1/employee/" + id);
    }
    //</editor-fold>

    //<editor-fold desc="PUT - Update Employee">
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        Integer id = parseEmployeeId(request.getPathInfo());

        if (id == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }
        if (!EmployeeStorage.employeeDB.containsKey(id)) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        Employee updatedEmployee = JsonUtil.fromJson(request.getReader(), Employee.class);
        updatedEmployee.setId(id);
        EmployeeStorage.employeeDB.put(id, updatedEmployee);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        setLocationHeader(request, response, "/api/v1/employee/" + id);
    }
    //</editor-fold>

    //<editor-fold desc="DELETE - Delete Employee">
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setJsonResponse(response);
        Integer id = parseEmployeeId(request.getPathInfo());

        if (id == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }
        if (!EmployeeStorage.employeeDB.containsKey(id)) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        EmployeeStorage.employeeDB.remove(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        setLocationHeader(request, response, "/api/v1/employees");
    }
    //</editor-fold>
}
