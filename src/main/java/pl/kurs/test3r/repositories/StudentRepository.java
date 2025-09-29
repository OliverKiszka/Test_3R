package pl.kurs.test3r.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.models.person.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
