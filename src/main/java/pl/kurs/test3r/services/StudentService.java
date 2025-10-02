package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;
import pl.kurs.test3r.dto.StudentDto;
import pl.kurs.test3r.models.person.Student;
import pl.kurs.test3r.services.crud.StudentCrudService;
@Service
@Transactional
public class StudentService {

    private final StudentCrudService studentCrudService;
    private final ModelMapper mapper;

    public StudentService(StudentCrudService studentCrudService, ModelMapper mapper) {
        this.studentCrudService = studentCrudService;
        this.mapper = mapper;
    }


    public StudentDto create(CreateStudentCommand createStudentCommand){
        Student student = mapper.map(createStudentCommand, Student.class);
        Student savedStudent = studentCrudService.add(student);
        return mapper.map(savedStudent, StudentDto.class);
    }

    public StudentDto update(UpdateStudentCommand updateStudentCommand) {
        Student student = mapper.map(updateStudentCommand, Student.class);
        student.setId(updateStudentCommand.getId());
        student.setVersion(updateStudentCommand.getVersion());
        Student updatedStudent = studentCrudService.edit(student);
        return mapper.map(updatedStudent, StudentDto.class);
    }

}
