package pl.kurs.test3r.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kurs.test3r.commands.CreatePositionCommand;
import pl.kurs.test3r.commands.UpdatePositionCommand;
import pl.kurs.test3r.dto.PositionHistoryDto;
import pl.kurs.test3r.exceptions.IllegalEntityStateException;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.position.PositionHistory;
import pl.kurs.test3r.repositories.EmployeeRepository;
import pl.kurs.test3r.repositories.PositionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false"
})
public class PositionServiceTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Long employeeId;

    @BeforeEach
    void setUp() {
        positionRepository.deleteAll();
        employeeRepository.deleteAll();
        Employee employee = createEmployee();
        employeeId = employee.getId();
    }

    @AfterEach
    void tearDown() {
        positionRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void shouldCreatePositionAndUpdateEmployeeState() {
        CreatePositionCommand command = createCommand("Developer", 6000.0,
                LocalDate.of(2020, 1, 1), null);

        PositionHistoryDto created = positionService.createPosition(employeeId, command);

        Employee refreshed = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(created.getId()).isNotNull();
        assertThat(refreshed.getCurrentPosition()).isEqualTo("Developer");
        assertThat(refreshed.getCurrentSalary()).isEqualTo(6000.0);
    }

    @Test
    void shouldRejectOverlappingPositionOnCreate() {
        positionService.createPosition(employeeId, createCommand("Developer", 6000.0,
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));

        CreatePositionCommand overlapping = createCommand("Senior Developer", 7000.0,
                LocalDate.of(2020, 6, 1), null);

        assertThatThrownBy(() -> positionService.createPosition(employeeId, overlapping))
                .isInstanceOf(IllegalEntityStateException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    void shouldRejectOverlappingPositionOnUpdate() {
        PositionHistoryDto first = positionService.createPosition(employeeId, createCommand("Developer", 6000.0,
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));
        PositionHistoryDto second = positionService.createPosition(employeeId, createCommand("Senior Developer", 7000.0,
                LocalDate.of(2021, 1, 1), null));

        UpdatePositionCommand update = new UpdatePositionCommand();
        update.setPositionName("Senior Developer");
        update.setSalary(7000.0);
        update.setDateFrom(LocalDate.of(2020, 6, 1));
        update.setDateTo(null);

        assertThatThrownBy(() -> positionService.updatePosition(employeeId, second.getId(), update))
                .isInstanceOf(IllegalEntityStateException.class)
                .hasMessageContaining("overlap");

        Employee refreshed = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(refreshed.getCurrentPosition()).isEqualTo("Senior Developer");
        assertThat(refreshed.getCurrentSalary()).isEqualTo(7000.0);
    }


    @Test
    void shouldHandleConcurrentInsertionsWithoutOverlaps() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCounter = new AtomicInteger();

        Runnable task = () -> {
            ready.countDown();
            try {
                start.await(5, TimeUnit.SECONDS);
                positionService.createPosition(employeeId, createCommand("Developer", 6000.0,
                        LocalDate.of(2020, 1, 1), null));
                successCounter.incrementAndGet();
            } catch (IllegalEntityStateException ignored) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Future<?> first = executor.submit(task);
        Future<?> second = executor.submit(task);
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        List<PositionHistory> positions = positionRepository.findAllByEmployeeIdOrderByDateFrom(employeeId);
        assertThat(positions).hasSize(1);
        assertThat(successCounter.get()).isEqualTo(1);

        Employee refreshed = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(refreshed.getCurrentPosition()).isEqualTo("Developer");
        assertThat(refreshed.getCurrentSalary()).isEqualTo(6000.0);
    }

    private Employee createEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setPesel(randomPesel());
        employee.setHeight(182.0);
        employee.setWeight(85.0);
        employee.setEmail(UUID.randomUUID() + "@example.com");
        employee.setStartDate(LocalDate.of(2019, 1, 1));
        employee.setCurrentPosition(null);
        employee.setCurrentSalary(0.0);
        return employeeRepository.saveAndFlush(employee);
    }

    private CreatePositionCommand createCommand(String name, double salary, LocalDate from, LocalDate to) {
        CreatePositionCommand command = new CreatePositionCommand();
        command.setPositionName(name);
        command.setSalary(salary);
        command.setDateFrom(from);
        command.setDateTo(to);
        return command;
    }

    private String randomPesel() {
        long number = Math.abs(UUID.randomUUID().getMostSignificantBits());
        number = number % 1_000_000_00000L;
        return String.format("%011d", number);
    }

}
