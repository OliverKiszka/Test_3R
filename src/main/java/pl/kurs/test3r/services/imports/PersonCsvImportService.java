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
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.repositories.ImportJobRepository;
import pl.kurs.test3r.services.person.PersonTypeModule;
import pl.kurs.test3r.services.person.PersonTypeRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Service
public class PersonCsvImportService {

    private static final Logger log = LoggerFactory.getLogger(PersonCsvImportService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final PersonTypeRegistry personTypeRegistry;
    private final ImportJobRepository importJobRepository;
    private final ImportProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final TaskExecutor taskExecutor;
    private final Semaphore concurrencySemaphore;

    @PersistenceContext
    private EntityManager entityManager;

    public PersonCsvImportService(ImportJobRepository importJobRepository,
                                  ImportProperties properties,
                                  PlatformTransactionManager transactionManager,
                                  @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                                  PersonTypeRegistry personTypeRegistry) {
        this.importJobRepository = importJobRepository;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.taskExecutor = taskExecutor;
        this.personTypeRegistry = personTypeRegistry;
        this.concurrencySemaphore = properties.getMaxConcurrentImports() > 0
                ? new Semaphore(properties.getMaxConcurrentImports(), true)
                : null;
    }

    public ImportJobDto startImport(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportProcessingException("Uploaded file must not be empty");
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
            String formattedThroughput = String.format(Locale.ROOT, "%.2f", summary.throughput());
            log.info("Import job {} completed. Processed {} records at {} rows/s", jobId, summary.totalRecords(), formattedThroughput);
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
        try (BufferedReader reader = Files.newBufferedReader(filepath, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ImportProcessingException("CSV file is empty");
            }

            String delimiter = headerLine.contains(";") ? ";" : ",";
            String[] headers = tokenize(headerLine, delimiter, 0);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);
            validateRequiredColumns(headerIndex);

            List<Person> batch = new ArrayList<>(batchSize);
            long processed = 0;
            long start = System.nanoTime();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = tokenize(line, delimiter, headers.length);
                Person person = mapToPerson(headerIndex, values, rowNumber);
                batch.add(person);
                processed++;
                if (batch.size() >= batchSize) {
                    persistBatch(batch);
                }
            }
            persistBatch(batch);
            double durationSeconds = Math.max(1e-9, (System.nanoTime() - start) / 1_000_000_000.0);
            double throughput = processed / durationSeconds;
            if (properties.getMinimumTps() > 0 && throughput < properties.getMinimumTps()) {
                log.warn("Import throughput {} rows/s below configured minimum {}", String.format(Locale.ROOT, "%.2f", throughput), properties.getMinimumTps());
            }
            if (processed > Integer.MAX_VALUE) {
                throw new ImportProcessingException("Imported record count exceeds supported limit");
            }
            return new ImportSummary((int) processed, throughput);
        } catch (IOException e) {
            throw new ImportProcessingException("Failed to read CSV file", e);
        }
    }

    private void persistBatch(List<Person> batch) {
        if (batch.isEmpty()) {
            return;
        }
        batch.forEach(entityManager::persist);
        entityManager.flush();
        entityManager.clear();
        batch.clear();
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].trim().toLowerCase(Locale.ROOT), i);
        }
        return index;
    }

    private void validateRequiredColumns(Map<String, Integer> headerIndex) {
        String[] required = {"type", "firstname", "lastname", "pesel", "height", "weight", "email"};
        for (String column : required) {
            if (!headerIndex.containsKey(column)) {
                throw new ImportProcessingException("Missing required column: " + column);
            }
        }
    }

    private Person mapToPerson(Map<String, Integer> headerIndex, String[] values, int rowNumber) {
        Map<String, String> normalizedValues = extractValues(headerIndex, values);
        PersonCsvRow row = new PersonCsvRow(normalizedValues, rowNumber);
        PersonTypeModule<?, ?, ?> module = personTypeRegistry.getByType(row.type());
        Person person = module.createFromCsv(row);
        return person;
    }

    private Map<String, String> extractValues(Map<String, Integer> headerIndex, String[] values) {
        Map<String, String> normalized = new HashMap<>();
        headerIndex.forEach((column, index) -> {
            String value = index != null && index < values.length ? values[index] : "";
            normalized.put(column, value == null ? "" : value.trim());
        });
        return normalized;
    }


    private String[] tokenize(String line, String delimiter, int expectedLength) {
        String[] tokens = line.split(Pattern.quote(delimiter), -1);
        if (expectedLength > 0 && tokens.length < expectedLength) {
            tokens = java.util.Arrays.copyOf(tokens, expectedLength);
        }
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == null) {
                tokens[i] = "";
            }
            tokens[i] = tokens[i].trim();
        }
        return tokens;
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

    private record ImportSummary(int totalRecords, double throughput) {
    }
}
