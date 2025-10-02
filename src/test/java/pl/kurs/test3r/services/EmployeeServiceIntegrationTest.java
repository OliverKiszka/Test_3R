package pl.kurs.test3r.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.UpdateEmployeeCommand;
import pl.kurs.test3r.dto.EmployeeDto;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.position.PositionHistory;
import pl.kurs.test3r.repositories.EmployeeRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@Transactional
public class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldSetCountsWhenCreatingEmployee() {
        EmployeeDto created = employeeService.create(createEmployeeCommand());

        assertThat(created.getPositionCount()).isZero();
        assertThat(created.getProfessionCount()).isZero();
        assertThat(created.getType()).isEqualTo("EMPLOYEE");
    }

    @Test
    void shouldCountPositionsAndProfessionsAfterUpdate() {
        EmployeeDto created = employeeService.create(createEmployeeCommand());
        Employee employee = employeeRepository.findById(created.getId()).orElseThrow();
        employee.getPositions().clear();

        employee.getPositions().addAll(List.of(
                position(employee, "Developer", LocalDate.of(2020, 1, 1)),
                position(employee, "developer", LocalDate.of(2021, 1, 1)),
                position(employee, "Architect", LocalDate.of(2022, 6, 1))
        ));
        employeeRepository.saveAndFlush(employee);

        UpdateEmployeeCommand updateCommand = updateEmployeeCommand(created);
        updateCommand.setCurrentPosition("Principal Architect");
        updateCommand.setCurrentSalary(created.getCurrentSalary() + 1500.0);

        EmployeeDto updated = employeeService.update(updateCommand);

        assertThat(updated.getPositionCount()).isEqualTo(3);
        assertThat(updated.getProfessionCount()).isEqualTo(2);
    }

    private CreateEmployeeCommand createEmployeeCommand() {
        CreateEmployeeCommand command = new CreateEmployeeCommand();
        command.setType("EMPLOYEE");
        command.setFirstName("John");
        command.setLastName("Doe");
        command.setPesel("12345678901");
        command.setHeight(182.0);
        command.setWeight(85.0);
        command.setEmail("john.doe@example.com");
        command.setStartDate(LocalDate.of(2019, 5, 1));
        command.setCurrentPosition("Developer");
        command.setCurrentSalary(6000.0);
        return command;
    }

    private UpdateEmployeeCommand updateEmployeeCommand(EmployeeDto base) {
        UpdateEmployeeCommand command = new UpdateEmployeeCommand();
        command.setType("EMPLOYEE");
        command.setId(base.getId());
        command.setVersion(base.getVersion());
        command.setFirstName(base.getFirstName());
        command.setLastName(base.getLastName());
        command.setPesel(base.getPesel());
        command.setHeight(base.getHeight());
        command.setWeight(base.getWeight());
        command.setEmail(base.getEmail());
        command.setStartDate(base.getStartDate());
        command.setCurrentPosition(base.getCurrentPosition());
        command.setCurrentSalary(base.getCurrentSalary());
        return command;
    }

    private PositionHistory position(Employee employee, String name, LocalDate from) {
        PositionHistory history = new PositionHistory();
        history.setEmployee(employee);
        history.setPositionName(name);
        history.setSalary(5000.0);
        history.setDateFrom(from);
        return history;
    }

}
