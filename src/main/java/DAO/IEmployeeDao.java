package dao;
//<editor-fold desc="Imports">
import model.Employee;

import java.util.Optional;
//</editor-fold>

public interface IEmployeeDao extends IGenericDao<Employee, Long> {

  Optional<Employee> findByIdWithHobbies(Long id);
}
