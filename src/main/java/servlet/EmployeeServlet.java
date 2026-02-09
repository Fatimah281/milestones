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

@WebServlet("/api/v1/employees/*")
public class EmployeeServlet extends HttpServlet {

    //<editor-fold desc="GET - Read Employee(s)">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        // GET ALL: /api/v1/employees
        if (pathInfo == null || pathInfo.equals("/")) {
            Collection<Employee> employees = EmployeeStorage.employeeDB.values();
            response.getWriter().write(JsonUtil.toJson(employees));
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // GET BY ID: /api/v1/employees/{id}
        try {
            int id = Integer.parseInt(pathInfo.replace("/", ""));
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
        response.getWriter().write(JsonUtil.toJson(employee));
    }
    //</editor-fold>
    //<editor-fold desc="PUT - Update Employee">
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        System.out.println("pathInfo = " + request.getPathInfo());

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Employee ID is required\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.replace("/", ""));

            if (!EmployeeStorage.employeeDB.containsKey(id)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Employee not found\"}");
                return;
            }

            Employee updatedEmployee = JsonUtil.fromJson(request.getReader(), Employee.class);
            updatedEmployee.setId(id);

            EmployeeStorage.employeeDB.put(id, updatedEmployee);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(JsonUtil.toJson(updatedEmployee));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid ID format\"}");
        }
    }
    //</editor-fold>
    //<editor-fold desc="DELETE - Delete Employee">
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Employee ID is required\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.replace("/", ""));

            if (!EmployeeStorage.employeeDB.containsKey(id)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Employee not found\"}");
                return;
            }

            EmployeeStorage.employeeDB.remove(id);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid ID format\"}");
        }
    }
    //</editor-fold>
}
