package pl.kurs.test3r.services.crud;

import org.springframework.stereotype.Service;
import pl.kurs.test3r.models.person.Student;
import pl.kurs.test3r.repositories.StudentRepository;

@Service
public class StudentCrudService extends GenericCrudService<Student, Long, StudentRepository> {
    public StudentCrudService(StudentRepository repository) {
        super(repository);
    }
}
