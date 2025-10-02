package pl.kurs.test3r.services.imports;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3r.config.ImportProperties;
import pl.kurs.test3r.dto.ImportJobDto;
import pl.kurs.test3r.exceptions.ImportConcurrencyException;
import pl.kurs.test3r.exceptions.ImportJobNotFoundException;
import pl.kurs.test3r.exceptions.ImportProcessingException;
import pl.kurs.test3r.models.imports.ImportJob;
import pl.kurs.test3r.models.imports.ImportJobStatus;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.models.person.Retiree;
import pl.kurs.test3r.models.person.Student;
import pl.kurs.test3r.repositories.ImportJobRepository;
import pl.kurs.test3r.repositories.PersonRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PersonCsvImportService {

    private static final Logger log = LoggerFactory.getLogger(PersonCsvImportService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final PersonRepository personRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final TaskExecutor taskExecutor;
    private final Semaphore concurrencySemaphore;

    @PersistenceContext
    private EntityManager entityManager;

    public PersonCsvImportService(PersonRepository personRepository,
                                  ImportJobRepository importJobRepository,
                                  ImportProperties properties,
                                  PlatformTransactionManager transactionManager,
                                  @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.personRepository = personRepository;
        this.importJobRepository = importJobRepository;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.taskExecutor = taskExecutor;
        this.concurrencySemaphore = properties.getMaxConcurrentImports() > 0 ? new Semaphore(properties.getMaxConcurrentImports(), true) : null;
    }

    public ImportJobDto startImport(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportProcessingException("Umpoaded file must not be empty");
        }

        AtomicBoolean permitHolder = new AtomicBoolean(false);
        if (concurrencySemaphore != null) {
            permitHolder.set(concurrencySemaphore.tryAcquire());
            if (!permitHolder.get()) {
                throw new ImportConcurrencyException(properties.getMaxConcurrentImports());
            }
        }

        try {
            ImportJob job = new ImportJob(file.getOriginalFilename(), file.getSize(), file.getContentType());
            ImportJob saved = importJobRepository.save(job);

            Path tempFile = Files.createTempFile("persons-import-", saved.getId() + ".csv");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            taskExecutor.execute(() -> processImport(saved.getId(), tempFile, permitHolder.get()));
            permitHolder.set(false);

            return ImportJobDto.from(saved);

        } catch (IOException ex) {
            throw new ImportProcessingException("Failed to store uploaded file", ex);
        } finally {
            if (permitHolder.get() && concurrencySemaphore != null) {
                concurrencySemaphore.release();
            }
        }
    }

    public ImportJobDto getStatus(Long id) {
        ImportJob job = importJobRepository.findById(id)
                .orElseThrow(() -> new ImportJobNotFoundException(id));
        return ImportJobDto.from(job);
    }

    private void processImport(Long jobId, Path filePath, boolean releasePermit) {
        try {
            markInProgress(jobId);
            ImportSummary summary = runImportInTransaction(filePath);
            markCompleted(jobId, summary);
        } catch (Exception ex) {
            log.error("Import job {} failed", jobId, ex);
            markFailed(jobId, ex);
        } finally {
            if (releasePermit && concurrencySemaphore != null) {
                concurrencySemaphore.release();
            }
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Could not delete temporary file {}", filePath, e);
            }
        }
    }

    private ImportSummary runImportInTransaction(Path filepath) {
        return transactionTemplate.execute(status -> doImport(filepath));
    }

    private ImportSummary doImport(Path filepath) {
        int batchSize = Math.max(1, properties.getBatchSize());
        try (
                BufferedReader reader = Files.newBufferedReader(filepath, StandardCharsets.UTF_8)
        ) {
            String headerLine = reader.readLine();
            if (headerLine == null){
                throw new ImportProcessingException("CSV file is empty");
            }

            String delimiter = headerLine.contains(";") ? ";" : ",";
            String[] headers = tokenize(headerLine, delimiter, 0);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);
            validateRequiredColumns(headerIndex);

            List<Person> batch = new ArrayList<>(batchSize);
            int processed = 0;
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null){
                rowNumber++;
                if (line.isBlank()){
                    continue;
                }
                String[] values = tokenize(line, delimiter, headers.length);
                Person person = mapToPerson(headerIndex, values, rowNumber);
                batch.add(person);
                processed++;
                if (batch.size() >= batchSize){
                    persistBatch(batch);
                }
            }
            persistBatch(batch);
            return new ImportSummary(processed);
        } catch (IOException e){
            throw new ImportProcessingException("Failed to read CSV file", e);
        }
    }

    private void persistBatch(List<Person> batch){
        if (batch.isEmpty()){
            return;
        }
        personRepository.saveAll(batch);
        personRepository.flush();
        batch.forEach(entityManager::detach);
        batch.clear();
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers){
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++){
            index.put(headers[i].trim().toLowerCase(Locale.ROOT),i);
        }
        return index;
    }

    private void validateRequiredColumns(Map<String, Integer> headerIndex){
        String[] required = {"type", "firstname", "lastname", "pesel", "height", "weight", "email"};
        for (String column : required){
            if (!headerIndex.containsKey(column)){
                throw new ImportProcessingException("Missing required column: " + column);
            }
        }
    }

    private Person mapToPerson(Map<String, Integer> headerIndex, String[] values, int rowNumber){
        String type = requiredValue(headerIndex, values, "type", rowNumber).toUpperCase(Locale.ROOT);
        String firstName = requiredValue(headerIndex, values, "firstname", rowNumber);
        String lastName = requiredValue(headerIndex, values, "lastname", rowNumber);
        String pesel = requiredValue(headerIndex, values, "pesel", rowNumber);
        double height = parseDouble(requiredValue(headerIndex, values, "height", rowNumber), "height", rowNumber);
        double weight = parseDouble(requiredValue(headerIndex, values, "weight", rowNumber), "weight", rowNumber);
        String email = requiredValue(headerIndex, values, "email", rowNumber);

        return switch (type) {
            case "STUDENT" -> createStudent(headerIndex, values, rowNumber, firstName, lastName, pesel, height, weight, email);
            case "EMPLOYEE" -> createEmployee(headerIndex, values, rowNumber, firstName, lastName, pesel, height, weight, email);
            case "RETIREE" -> createRetiree(headerIndex, values, rowNumber, firstName, lastName, pesel, height, weight, email);
            default -> throw new ImportProcessingException("Unsupported person type '" + type + "' at row " + rowNumber);
        };
    }
    private Student createStudent(Map<String, Integer> headerIndex, String[] values, int rowNumber,
                                  String firstName, String lastName, String pesel,
                                  double height, double weight, String email) {
        Student student = new Student();
        applyBaseAttributes(student, firstName, lastName, pesel, height, weight, email);
        student.setUniversity(requiredValue(headerIndex, values, "university", rowNumber));
        student.setStudyYear(parseInt(requiredValue(headerIndex, values, "studyyear", rowNumber), "studyYear", rowNumber));
        student.setFieldOfStudy(requiredValue(headerIndex, values, "fieldofstudy", rowNumber));
        student.setScholarship(parseDouble(requiredValue(headerIndex, values, "scholarship", rowNumber), "scholarship", rowNumber));
        return student;
    }

    private Employee createEmployee(Map<String, Integer> headerIndex, String[] values, int rowNumber,
                                    String firstName, String lastName, String pesel,
                                    double height, double weight, String email) {
        Employee employee = new Employee();
        applyBaseAttributes(employee, firstName, lastName, pesel, height, weight, email);
        String startDate = requiredValue(headerIndex, values, "startdate", rowNumber);
        employee.setStartDate(parseDate(startDate, "startDate", rowNumber));
        employee.setCurrentPosition(requiredValue(headerIndex, values, "currentposition", rowNumber));
        employee.setCurrentSalary(parseDouble(requiredValue(headerIndex, values, "currentsalary", rowNumber), "currentSalary", rowNumber));
        return employee;
    }

    private Retiree createRetiree(Map<String, Integer> headerIndex, String[] values, int rowNumber,
                                  String firstName, String lastName, String pesel,
                                  double height, double weight, String email) {
        Retiree retiree = new Retiree();
        applyBaseAttributes(retiree, firstName, lastName, pesel, height, weight, email);
        retiree.setPensionAmount(parseDouble(requiredValue(headerIndex, values, "pensionamount", rowNumber), "pensionAmount", rowNumber));
        retiree.setYearsWorked(parseInt(requiredValue(headerIndex, values, "yearsworked", rowNumber), "yearsWorked", rowNumber));
        return retiree;
    }
    private void applyBaseAttributes(Person person, String firstName, String lastName, String pesel,
                                     double height, double weight, String email) {
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setPesel(pesel);
        person.setHeight(height);
        person.setWeight(weight);
        person.setEmail(email);
    }
    private String[] tokenize(String line, String delimiter, int expectedLength) {
        String[] tokens = line.split(Pattern.quote(delimiter), -1);
        if (expectedLength > 0 && tokens.length < expectedLength) {
            tokens = Arrays.copyOf(tokens, expectedLength);
        }
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == null) {
                tokens[i] = "";
            }
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }
    private String requiredValue(Map<String, Integer> headerIndex, String[] values, String column, int rowNumber) {
        Integer index = headerIndex.get(column);
        String value = (index != null && index < values.length) ? values[index] : "";
        if (value == null || value.isBlank()) {
            throw new ImportProcessingException("Missing value for column '" + column + "' at row " + rowNumber);
        }
        return value.trim();
    }
    private double parseDouble(String value, String column, int rowNumber) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ImportProcessingException("Invalid decimal value in column '" + column + "' at row " + rowNumber);
        }
    }
    private int parseInt(String value, String column, int rowNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ImportProcessingException("Invalid integer value in column '" + column + "' at row " + rowNumber);
        }
    }
    private LocalDate parseDate(String value, String column, int rowNumber) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ImportProcessingException("Invalid date value in column '" + column + "' at row " + rowNumber + "'. Expected format ISO-8601 (yyyy-MM-dd)");
        }
    }
    private void markInProgress(Long jobId) {
        transactionTemplate.executeWithoutResult(status -> {
            ImportJob job = importJobRepository.findById(jobId)
                    .orElseThrow(() -> new ImportJobNotFoundException(jobId));
            job.setStatus(ImportJobStatus.IN_PROGRESS);
            job.setStartedAt(OffsetDateTime.now());
            job.setFinishedAt(null);
            job.setErrorMessage(null);
            job.setProcessedRecords(0);
            job.setTotalRecords(null);
        });
    }
    private void markCompleted(Long jobId, ImportSummary summary) {
        transactionTemplate.executeWithoutResult(status -> {
            ImportJob job = importJobRepository.findById(jobId)
                    .orElseThrow(() -> new ImportJobNotFoundException(jobId));
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setFinishedAt(OffsetDateTime.now());
            job.setProcessedRecords(summary.totalRecords());
            job.setTotalRecords(summary.totalRecords());
            job.setErrorMessage(null);
        });
    }
    private void markFailed(Long jobId, Throwable throwable) {
        transactionTemplate.executeWithoutResult(status -> {
            ImportJob job = importJobRepository.findById(jobId)
                    .orElseThrow(() -> new ImportJobNotFoundException(jobId));
            job.setStatus(ImportJobStatus.FAILED);
            job.setFinishedAt(OffsetDateTime.now());
            job.setProcessedRecords(null);
            job.setTotalRecords(null);
            job.setErrorMessage(truncateMessage(throwable.getMessage()));
        });
    }
    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private record ImportSummary(int totalRecords) {
    }


}
