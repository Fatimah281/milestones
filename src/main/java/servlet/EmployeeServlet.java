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

    //<editor-fold desc="GET - Read Employee(s)">
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if ("/api/v1/employees".equals(servletPath)) {
            Collection<Employee> employees = EmployeeStorage.employeeDB.values();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(employees));
            return;
        }

        if ("/api/v1/employee".equals(servletPath)) {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Employee ID is required\"}");
                return;
            }

            // Extract ID from path
            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Invalid path format\"}");
                return;
            }

            try {
                int id = Integer.parseInt(parts[1]);
                Employee employee = EmployeeStorage.employeeDB.get(id);

                if (employee == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\":\"Employee not found\"}");
                    return;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(JsonUtil.toJson(employee));

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Invalid ID format\"}");
            }
            return;
        }

        // ----- INVALID PATH -----
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("{\"message\":\"Endpoint not found\"}");
    }
    //</editor-fold>
    //<editor-fold desc="POST - Create Employee">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Employee employee = JsonUtil.fromJson(request.getReader(), Employee.class);

        int id = EmployeeStorage.idCounter.getAndIncrement();
        employee.setId(id);
        EmployeeStorage.employeeDB.put(id, employee);

        response.setStatus(HttpServletResponse.SC_CREATED);

        String location = request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort()
                + request.getContextPath()
                + "/api/v1/employee/" + id;

        response.setHeader("Location", location);
    }
    //</editor-fold>
    //<editor-fold desc="PUT - Update Employee">
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Employee ID is required\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid path format\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
            if (!EmployeeStorage.employeeDB.containsKey(id)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Employee not found\"}");
                return;
            }

            Employee updatedEmployee = JsonUtil.fromJson(request.getReader(), Employee.class);
            updatedEmployee.setId(id);
            EmployeeStorage.employeeDB.put(id, updatedEmployee);

            // 204 No Content + Location header
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            String location = request.getScheme() + "://"
                    + request.getServerName() + ":"
                    + request.getServerPort()
                    + request.getContextPath()
                    + "/api/v1/employee/" + id;
            response.setHeader("Location", location);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid ID format\"}");
        }
    }
    //</editor-fold>
    //<editor-fold desc="DELETE - Delete Employee">
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Employee ID is required\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid path format\"}");
            return;
        }

        try {
            int id = Integer.parseInt(parts[1]);
            if (!EmployeeStorage.employeeDB.containsKey(id)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Employee not found\"}");
                return;
            }

            EmployeeStorage.employeeDB.remove(id);

            // 204 No Content + Location header pointing to collection
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            String location = request.getScheme() + "://"
                    + request.getServerName() + ":"
                    + request.getServerPort()
                    + request.getContextPath()
                    + "/api/v1/employees";
            response.setHeader("Location", location);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid ID format\"}");
        }
    }
    //</editor-fold>
}
