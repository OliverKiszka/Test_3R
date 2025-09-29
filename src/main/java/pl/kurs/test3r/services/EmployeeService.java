package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.UpdateEmployeeCommand;
import pl.kurs.test3r.dto.EmployeeDto;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.services.crud.EmployeeCrudService;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeCrudService employeeCrudService;
    private final ModelMapper mapper;

    public EmployeeService(EmployeeCrudService employeeCrudService, ModelMapper mapper) {
        this.employeeCrudService = employeeCrudService;
        this.mapper = mapper;
    }
    public EmployeeDto create(CreateEmployeeCommand createEmployeeCommand){
        Employee employee = mapper.map(createEmployeeCommand, Employee.class);
        return mapper.map(employee, EmployeeDto.class);
    }
    public EmployeeDto update(UpdateEmployeeCommand updateEmployeeCommand){
        Employee employee = mapper.map(updateEmployeeCommand, Employee.class);
        employee.setId(updateEmployeeCommand.getId());
        employee.setVersion(updateEmployeeCommand.getVersion());
        employeeCrudService.edit(employee);
        return mapper.map(employee, EmployeeDto.class);
    }


}
