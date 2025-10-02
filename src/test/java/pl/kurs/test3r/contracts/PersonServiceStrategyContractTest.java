package pl.kurs.test3r.contracts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kurs.test3r.commands.CreatePersonCommand;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdatePersonCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.StudentDto;
import pl.kurs.test3r.services.EmployeeService;
import pl.kurs.test3r.services.PersonService;
import pl.kurs.test3r.services.RetireeService;
import pl.kurs.test3r.services.StudentService;
import pl.kurs.test3r.services.strategy.CreatePersonStrategy;
import pl.kurs.test3r.services.strategy.EmployeePersonStrategy;
import pl.kurs.test3r.services.strategy.RetireePersonStrategy;
import pl.kurs.test3r.services.strategy.StudentPersonStrategy;
import pl.kurs.test3r.services.strategy.UpdatePersonStrategy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class PersonServiceStrategyContractTest {
    private StudentService studentService;
    private EmployeeService employeeService;
    private RetireeService retireeService;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        studentService = mock(StudentService.class);
        employeeService = mock(EmployeeService.class);
        retireeService = mock(RetireeService.class);

        StudentPersonStrategy studentStrategy = new StudentPersonStrategy(studentService);
        EmployeePersonStrategy employeeStrategy = new EmployeePersonStrategy(employeeService);
        RetireePersonStrategy retireeStrategy = new RetireePersonStrategy(retireeService);

        personService = new PersonService(
                List.of(studentStrategy, employeeStrategy, retireeStrategy),
                List.of(studentStrategy, employeeStrategy, retireeStrategy)
        );
    }

    @Test
    void shouldDelegateCreateToMatchingStrategy() {
        CreateStudentCommand command = new CreateStudentCommand();
        command.setType("STUDENT");
        StudentDto expected = new StudentDto();

        when(studentService.create(command)).thenReturn(expected);

        PersonDto result = personService.create(command);

        assertThat(result).isSameAs(expected);
        verify(studentService).create(command);
        verifyNoInteractions(employeeService, retireeService);
    }

    @Test
    void shouldDelegateUpdateToMatchingStrategy() {
        UpdateStudentCommand command = new UpdateStudentCommand();
        command.setType("STUDENT");
        StudentDto expected = new StudentDto();

        when(studentService.update(command)).thenReturn(expected);

        PersonDto result = personService.update(command);

        assertThat(result).isSameAs(expected);
        verify(studentService).update(command);
        verifyNoInteractions(employeeService, retireeService);
    }

    @Test
    void shouldAllowRegisteringNewStrategiesWithoutChangingPersonService() {
        DummyCreateCommand createCommand = new DummyCreateCommand();
        createCommand.setType("VOLUNTEER");
        DummyUpdateCommand updateCommand = new DummyUpdateCommand();
        updateCommand.setType("VOLUNTEER");

        DummyPersonDto createResult = new DummyPersonDto();
        DummyPersonDto updateResult = new DummyPersonDto();

        DummyStrategy dummyStrategy = new DummyStrategy(createResult, updateResult);

        PersonService extendedService = new PersonService(List.of(dummyStrategy), List.of(dummyStrategy));

        assertThat(extendedService.create(createCommand)).isSameAs(createResult);
        assertThat(extendedService.update(updateCommand)).isSameAs(updateResult);
    }

    private static class DummyCreateCommand extends CreatePersonCommand { }

    private static class DummyUpdateCommand extends UpdatePersonCommand { }

    private static class DummyPersonDto extends PersonDto { }

    private static class DummyStrategy implements CreatePersonStrategy<DummyCreateCommand>, UpdatePersonStrategy<DummyUpdateCommand> {

        private final PersonDto createResult;
        private final PersonDto updateResult;

        private DummyStrategy(PersonDto createResult, PersonDto updateResult) {
            this.createResult = createResult;
            this.updateResult = updateResult;
        }

        @Override
        public Class<DummyCreateCommand> getSupportedCreateCommand() {
            return DummyCreateCommand.class;
        }

        @Override
        public PersonDto create(DummyCreateCommand command) {
            return createResult;
        }

        @Override
        public Class<DummyUpdateCommand> getSupportedUpdateCommand() {
            return DummyUpdateCommand.class;
        }

        @Override
        public PersonDto update(DummyUpdateCommand command) {
            return updateResult;
        }
    }
}
