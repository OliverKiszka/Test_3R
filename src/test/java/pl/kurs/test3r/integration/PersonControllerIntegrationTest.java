package pl.kurs.test3r.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.CreateRetireeCommand;
import pl.kurs.test3r.repositories.PersonRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void shouldPersistEmployeeAfterPost() throws Exception {
        CreateEmployeeCommand command = new CreateEmployeeCommand();
        command.setType("EMPLOYEE");
        command.setFirstName("John");
        command.setLastName("Doe");
        command.setPesel("12345678901");
        command.setHeight(180.0);
        command.setWeight(80.0);
        command.setEmail("john.doe@example.com");
        command.setStartDate(LocalDate.of(2020, 1, 1));
        command.setCurrentPosition("Developer");
        command.setCurrentSalary(5000.0);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());

        assertThat(personRepository.findByPesel(command.getPesel())).isPresent();
    }

    @Test
    void shouldPersistRetireeAfterPost() throws Exception {
        CreateRetireeCommand command = new CreateRetireeCommand();
        command.setType("RETIREE");
        command.setFirstName("Jane");
        command.setLastName("Smith");
        command.setPesel("10987654321");
        command.setHeight(165.0);
        command.setWeight(60.0);
        command.setEmail("jane.smith@example.com");
        command.setPensionAmount(2000.0);
        command.setYearsWorked(40);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());

        assertThat(personRepository.findByPesel(command.getPesel())).isPresent();
    }
}
