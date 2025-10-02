package pl.kurs.test3r.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import pl.kurs.test3r.dto.ImportJobDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.services.PersonQueryService;
import pl.kurs.test3r.services.PositionService;
import pl.kurs.test3r.services.imports.PersonCsvImportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.TestClockConfiguration.class)
public class SecurityIntegrationTest {

    private static final Instant BASE_TIME = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonQueryService personQueryService;

    @MockBean
    private PersonCsvImportService personCsvImportService;

    @MockBean
    private PositionService positionService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private MutableClock mutableClock;

    @BeforeEach
    void setUp() {
        mutableClock.setInstant(BASE_TIME);
        loginAttemptService.recordSuccessfulLogin("employee");
        loginAttemptService.recordSuccessfulLogin("importer");
        loginAttemptService.recordSuccessfulLogin("admin");

        when(personQueryService.search(any(PersonSearchCriteria.class), any(Pageable.class))).thenReturn(Page.empty());
        when(positionService.getPositions(anyLong())).thenReturn(List.of());
        when(personCsvImportService.startImport(any())).thenReturn(new ImportJobDto());
        when(personCsvImportService.getStatus(anyLong())).thenReturn(new ImportJobDto());
    }

    @Test
    void shouldAllowAdminToAccessPersonSearch() throws Exception {
        mockMvc.perform(get("/api/persons").with(httpBasic("admin", "adminPass")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidImporterFromAccessingPersonSearch() throws Exception {
        mockMvc.perform(get("/api/persons").with(httpBasic("importer", "importerPass")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowImporterToUploadPersons() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "persons.csv", "text/csv", "data".getBytes());
        mockMvc.perform(multipart("/api/imports/persons").file(file).with(httpBasic("importer", "importerPass")))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldLockAccountAfterFailedAttempts() throws Exception {
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(get("/api/persons").with(httpBasic("employee", "wrong")))
                    .andExpect(status().isUnauthorized());
        }
        assertThat(loginAttemptService.isAccountLocked("employee")).isTrue();

        mockMvc.perform(get("/api/persons").with(httpBasic("employee", "employeePass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUnlockAccountAfterLockDuration() throws Exception {
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(get("/api/persons").with(httpBasic("employee", "wrong")))
                    .andExpect(status().isUnauthorized());
        }

        mutableClock.advance(Duration.ofMinutes(10).plusSeconds(1));

        mockMvc.perform(get("/api/persons").with(httpBasic("employee", "employeePass")))
                .andExpect(status().isOk());

        assertThat(loginAttemptService.isAccountLocked("employee")).isFalse();
    }

    @TestConfiguration
    static class TestClockConfiguration {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(BASE_TIME, ZoneOffset.UTC);
        }
    }

}
