package pl.kurs.test3r.services.strategy;

import org.springframework.stereotype.Component;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.UpdateEmployeeCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.EmployeeService;
@Component
public class EmployeePersonStrategy implements CreatePersonStrategy<CreateEmployeeCommand>, UpdatePersonStrategy<UpdateEmployeeCommand> {

    private final EmployeeService employeeService;

    public EmployeePersonStrategy(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @Override
    public Class<CreateEmployeeCommand> getSupportedCreateCommand() {
        return CreateEmployeeCommand.class;
    }

    @Override
    public PersonDto create(CreateEmployeeCommand command) {
        return employeeService.create(command);
    }

    @Override
    public Class<UpdateEmployeeCommand> getSupportedUpdateCommand() {
        return UpdateEmployeeCommand.class;
    }

    @Override
    public PersonDto update(UpdateEmployeeCommand command) {
        return employeeService.update(command);
    }
}
