package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.*;
import pl.kurs.test3r.dto.PersonDto;

@Service
@Transactional
public class PersonService {

    private final StudentService studentService;
    private final EmployeeService employeeService;
    private final RetireeService retireeService;

    public PersonService(StudentService studentService, EmployeeService employeeService, RetireeService retireeService) {
        this.studentService = studentService;
        this.employeeService = employeeService;
        this.retireeService = retireeService;
    }


    public PersonDto create(CreatePersonCommand createPersonCommand){
        String type = createPersonCommand.getType().toUpperCase();
        if (createPersonCommand instanceof CreateStudentCommand createStudentCommand) {
            return studentService.create(createStudentCommand);
        } else if (createPersonCommand instanceof CreateEmployeeCommand createEmployeeCommand) {
            return employeeService.create(createEmployeeCommand);
        } else if (createPersonCommand instanceof CreateRetireeCommand createRetireeCommand) {
            return retireeService.create(createRetireeCommand);
        } else {
            throw new IllegalArgumentException("Unsupported person type: " + createPersonCommand.getClass().getSimpleName());
        }
    }
    public PersonDto update(UpdatePersonCommand updatePersonCommand){
        if (updatePersonCommand instanceof UpdateStudentCommand updateStudentCommand) {
            return studentService.update(updateStudentCommand);
        } else if (updatePersonCommand instanceof UpdateEmployeeCommand updateEmployeeCommand) {
            return employeeService.update(updateEmployeeCommand);
        } else if (updatePersonCommand instanceof UpdateRetireeCommand updateRetireeCommand) {
            return retireeService.update(updateRetireeCommand);
        } else {
            throw new IllegalArgumentException("Unsupported person type: " + updatePersonCommand.getClass().getSimpleName());
        }
    }
}
