package storage;

//<editor-fold desc="Imports">
import model.Employee;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
//</editor-fold>

public class EmployeeStorage {
    //<editor-fold desc="In-memory Storage and ID Counter">
    public static Map<Integer, Employee> employeeDB = new ConcurrentHashMap<>();
    public static AtomicInteger idCounter = new AtomicInteger(1);
    //</editor-fold>
}
