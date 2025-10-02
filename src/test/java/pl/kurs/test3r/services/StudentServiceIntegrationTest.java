package pl.kurs.test3r.services;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;
import pl.kurs.test3r.dto.StudentDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
public class StudentServiceIntegrationTest {


    @Autowired
    private StudentService studentService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    void shouldUseSingleSelectAndSingleUpdateWhenEditingStudent() {
        StudentDto created = studentService.create(createStudentCommand());

        statistics.clear();

        UpdateStudentCommand updateCommand = createUpdateCommand(created, created.getVersion());
        updateCommand.setUniversity("Warsaw University of Technology");
        updateCommand.setStudyYear(3);
        updateCommand.setFieldOfStudy("Mathematics");
        updateCommand.setScholarship(1500.0);
        updateCommand.setEmail("johnny.doe@example.com");
        updateCommand.setWeight(created.getWeight() + 5.0);
        updateCommand.setLastName("Doe-Updated");

        StudentDto updated = studentService.update(updateCommand);

        assertThat(statistics.getPrepareStatementCount())
                .as("Expected exactly one select and one update statement")
                .isEqualTo(2);
        assertThat(updated.getVersion()).isEqualTo(created.getVersion() + 1);
        assertThat(updated.getUniversity()).isEqualTo("Warsaw University of Technology");
        assertThat(updated.getLastName()).isEqualTo("Doe-Updated");
        assertThat(updated.getEmail()).isEqualTo("johnny.doe@example.com");
    }

    @Test
    void shouldPropagateOptimisticLockingFailureExceptionForStaleVersion() {
        StudentDto created = studentService.create(createStudentCommand());

        UpdateStudentCommand firstUpdate = createUpdateCommand(created, created.getVersion());
        firstUpdate.setEmail("johnny.doe@example.com");
        firstUpdate.setScholarship(created.getScholarship() + 200.0);
        StudentDto updated = studentService.update(firstUpdate);

        UpdateStudentCommand staleUpdate = createUpdateCommand(created, created.getVersion());
        staleUpdate.setEmail("johnny.duplicate@example.com");
        staleUpdate.setScholarship(created.getScholarship() + 300.0);

        assertThrows(OptimisticLockingFailureException.class, () -> studentService.update(staleUpdate));
        assertThat(updated.getVersion()).isEqualTo(created.getVersion() + 1);
    }

    private CreateStudentCommand createStudentCommand() {
        CreateStudentCommand command = new CreateStudentCommand();
        command.setType("STUDENT");
        command.setFirstName("John");
        command.setLastName("Doe");
        command.setPesel("12345678901");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("john.doe@example.com");
        command.setUniversity("University of Warsaw");
        command.setStudyYear(2);
        command.setFieldOfStudy("Computer Science");
        command.setScholarship(1000.0);
        return command;
    }

    private UpdateStudentCommand createUpdateCommand(StudentDto base, Long version) {
        UpdateStudentCommand command = new UpdateStudentCommand();
        command.setType("STUDENT");
        command.setId(base.getId());
        command.setVersion(version);
        command.setLastName(base.getLastName());
        command.setPesel(base.getPesel());
        command.setHeight(base.getHeight());
        command.setWeight(base.getWeight());
        command.setEmail(base.getEmail());
        command.setUniversity(base.getUniversity());
        command.setStudyYear(base.getStudyYear());
        command.setFieldOfStudy(base.getFieldOfStudy());
        command.setScholarship(base.getScholarship());
        return command;
    }

}
