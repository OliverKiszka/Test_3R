package pl.kurs.test3r.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.models.imports.ImportJob;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
}
