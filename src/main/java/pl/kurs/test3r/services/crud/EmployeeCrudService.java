package pl.kurs.test3r.services.crud;

import org.springframework.stereotype.Service;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.repositories.EmployeeRepository;

@Service
public class EmployeeCrudService extends GenericCrudService<Employee, Long, EmployeeRepository> {
    public EmployeeCrudService(EmployeeRepository repository) {
        super(repository);
    }
}
