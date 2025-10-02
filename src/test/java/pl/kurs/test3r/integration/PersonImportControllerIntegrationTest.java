package pl.kurs.test3r.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.test3r.config.ImportProperties;
import pl.kurs.test3r.dto.ImportJobDto;
import pl.kurs.test3r.models.imports.ImportJob;
import pl.kurs.test3r.models.imports.ImportJobStatus;
import pl.kurs.test3r.repositories.ImportJobRepository;
import pl.kurs.test3r.repositories.PersonRepository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PersonImportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private ImportProperties importProperties;

    @BeforeEach
    void cleanRepositories() {
        personRepository.deleteAll();
        importJobRepository.deleteAll();
    }

    @Test
    void shouldImportPersonsFromCsv() throws Exception {
        String header = "type,firstName,lastName,pesel,height,weight,email,startDate,currentPosition,currentSalary,university,studyYear,fieldOfStudy,scholarship,pensionAmount,yearsWorked";
        String studentRow = String.join(",",
                "STUDENT", "John", "Smith", "12345678901", "180", "75", "john.smith@example.com",
                "", "", "", "University of Testing", "1", "Computer Science", "1200", "", "");
        String employeeRow = String.join(",",
                "EMPLOYEE", "Anna", "Jones", "23456789012", "170", "60", "anna.jones@example.com",
                "2020-01-01", "Engineer", "6500", "", "", "", "", "", "");
        String retireeRow = String.join(",",
                "RETIREE", "Mark", "Brown", "34567890123", "175", "82", "mark.brown@example.com",
                "", "", "", "", "", "", "", "2500", "35");
        String csv = String.join("\n", header, studentRow, employeeRow, retireeRow);

        ImportJobDto jobDto = submitCsv(csv, "persons.csv");

        ImportJob completed = awaitJobCompletion(jobDto.getId());

        assertThat(completed.getStatus()).isEqualTo(ImportJobStatus.COMPLETED);
        assertThat(completed.getProcessedRecords()).isEqualTo(3);

        assertThat(personRepository.findByPesel("12345678901")).isPresent();
        assertThat(personRepository.findByPesel("23456789012")).isPresent();
        assertThat(personRepository.findByPesel("34567890123")).isPresent();
    }

    @Test
    void shouldSatisfyThroughputRequirement() throws Exception {
        int records = 200;
        String header = "type,firstName,lastName,pesel,height,weight,email,startDate,currentPosition,currentSalary,university,studyYear,fieldOfStudy,scholarship,pensionAmount,yearsWorked";
        StringBuilder builder = new StringBuilder(header).append('\n');
        for (int i = 0; i < records; i++) {
            String pesel = String.format("%011d", 50000000000L + i);
            String row = String.join(",",
                    "STUDENT",
                    "Name" + i,
                    "Test" + i,
                    pesel,
                    "180",
                    "75",
                    "student" + i + "@example.com",
                    "",
                    "",
                    "",
                    "University " + i,
                    String.valueOf(1 + (i % 5)),
                    "Field " + i,
                    "1500",
                    "",
                    "");
            builder.append(row).append('\n');
        }

        ImportJobDto jobDto = submitCsv(builder.toString(), "bulk-persons.csv");

        ImportJob completed = awaitJobCompletion(jobDto.getId());

        assertThat(completed.getStatus()).isEqualTo(ImportJobStatus.COMPLETED);
        assertThat(completed.getProcessedRecords()).isEqualTo(records);

        Duration duration = Duration.between(completed.getStartedAt(), completed.getFinishedAt());
        double seconds = Math.max(duration.toMillis() / 1000.0, 0.001);
        double tps = completed.getProcessedRecords() / seconds;

        assertThat(tps).isGreaterThanOrEqualTo(importProperties.getMinimumTps());
    }

    private ImportJobDto submitCsv(String csv, String fileName) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/api/imports/persons").file(file))
                .andExpect(status().isAccepted())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ImportJobDto.class);
    }

    private ImportJob awaitJobCompletion(Long jobId) throws Exception {
        long timeoutAt = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < timeoutAt) {
            MvcResult result = mockMvc.perform(get("/api/imports/persons/" + jobId))
                    .andExpect(status().isOk())
                    .andReturn();
            ImportJobDto dto = objectMapper.readValue(result.getResponse().getContentAsString(), ImportJobDto.class);
            ImportJob job = importJobRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalStateException("Import job disappeared"));
            if (job.getStatus() == ImportJobStatus.COMPLETED || job.getStatus() == ImportJobStatus.FAILED) {
                return job;
            }
            Thread.sleep(100);
        }
        fail("Import job did not finish within timeout");
        return null;
    }
}
