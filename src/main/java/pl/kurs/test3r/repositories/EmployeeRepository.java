package pl.kurs.test3r.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.models.person.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
