package pl.kurs.test3r.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.models.person.Retiree;
import pl.kurs.test3r.models.person.Student;
import pl.kurs.test3r.models.position.PositionHistory;
import pl.kurs.test3r.repositories.PersonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PersonQueryControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        personRepository.flush();
        personRepository.saveAll(seedPersons());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/persons")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "firstName,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.content[1].firstName").value("Bob"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldFilterEmployeesBySalaryAndProfessionCount() throws Exception {
        mockMvc.perform(get("/api/persons")
                        .param("type", "EMPLOYEE")
                        .param("salaryFrom", "6500")
                        .param("professionCountFrom", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.content[0].positionCount").value(2))
                .andExpect(jsonPath("$.content[0].professionCount").value(2));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldFilterEmployeesByProfessionCountRange() throws Exception {
        mockMvc.perform(get("/api/persons")
                        .param("type", "EMPLOYEE")
                        .param("professionCountFrom", "1")
                        .param("professionCountTo", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Bob"))
                .andExpect(jsonPath("$.content[0].positionCount").value(1))
                .andExpect(jsonPath("$.content[0].professionCount").value(1));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldFilterStudentsByUniversityAndScholarship() throws Exception {
        mockMvc.perform(get("/api/persons")
                        .param("type", "STUDENT")
                        .param("university", "Warsaw University")
                        .param("scholarshipTo", "1500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Charlie"))
                .andExpect(jsonPath("$.content[0].university").value("Warsaw University"));
    }

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/persons").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private List<Person> seedPersons() {
        Employee alice = createEmployee(
                "Alice", "Smith", "11111111111", "alice@example.com",
                LocalDate.of(2018, 1, 1), "Lead Developer", 7200.0,
                List.of(
                        createPosition("Developer", 6000.0, LocalDate.of(2018, 1, 1), LocalDate.of(2019, 12, 31)),
                        createPosition("Architect", 7200.0, LocalDate.of(2020, 1, 1), null)
                ));

        Employee bob = createEmployee(
                "Bob", "Taylor", "22222222222", "bob@example.com",
                LocalDate.of(2021, 5, 10), "QA Engineer", 4500.0,
                List.of(createPosition("Tester", 4200.0, LocalDate.of(2021, 5, 10), null)));

        Student charlie = createStudent(
                "Charlie", "Johnson", "33333333333", "charlie@example.com",
                "Warsaw University", 2, "Computer Science", 1200.0);

        Retiree diana = createRetiree(
                "Diana", "Prince", "44444444444", "diana@example.com",
                2500.0, 38);

        return List.of(alice, bob, charlie, diana);
    }

    private Employee createEmployee(String firstName, String lastName, String pesel, String email,
                                    LocalDate startDate, String currentPosition, double currentSalary,
                                    List<PositionHistory> positions) {
        Employee employee = new Employee();
        populateCommonFields(employee, firstName, lastName, pesel, email, 180.0, 75.0);
        employee.setStartDate(startDate);
        employee.setCurrentPosition(currentPosition);
        employee.setCurrentSalary(currentSalary);
        employee.setPositions(new ArrayList<>());
        for (PositionHistory position : positions) {
            position.setEmployee(employee);
            employee.getPositions().add(position);
        }
        return employee;
    }

    private Student createStudent(String firstName, String lastName, String pesel, String email,
                                  String university, int studyYear, String fieldOfStudy, double scholarship) {
        Student student = new Student();
        populateCommonFields(student, firstName, lastName, pesel, email, 175.0, 68.0);
        student.setUniversity(university);
        student.setStudyYear(studyYear);
        student.setFieldOfStudy(fieldOfStudy);
        student.setScholarship(scholarship);
        return student;
    }

    private Retiree createRetiree(String firstName, String lastName, String pesel, String email,
                                  double pensionAmount, int yearsWorked) {
        Retiree retiree = new Retiree();
        populateCommonFields(retiree, firstName, lastName, pesel, email, 168.0, 70.0);
        retiree.setPensionAmount(pensionAmount);
        retiree.setYearsWorked(yearsWorked);
        return retiree;
    }

    private void populateCommonFields(Person person, String firstName, String lastName, String pesel, String email,
                                      double height, double weight) {
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPesel(pesel);
        person.setEmail(email);
        person.setHeight(height);
        person.setWeight(weight);
    }

    private PositionHistory createPosition(String name, double salary, LocalDate from, LocalDate to) {
        PositionHistory positionHistory = new PositionHistory();
        positionHistory.setPositionName(name);
        positionHistory.setSalary(salary);
        positionHistory.setDateFrom(from);
        positionHistory.setDateTo(to);
        return positionHistory;
    }
}
