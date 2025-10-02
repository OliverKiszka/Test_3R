package pl.kurs.test3r.services.strategy;

import org.springframework.stereotype.Component;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.StudentService;

@Component
public class StudentPersonStrategy implements CreatePersonStrategy<CreateStudentCommand>, UpdatePersonStrategy<UpdateStudentCommand> {

    private final StudentService studentService;

    public StudentPersonStrategy(StudentService studentService) {
        this.studentService = studentService;
    }


    @Override
    public Class<CreateStudentCommand> getSupportedCreateCommand() {
        return CreateStudentCommand.class;
    }

    @Override
    public PersonDto create(CreateStudentCommand command) {
        return studentService.create(command);
    }

    @Override
    public Class<UpdateStudentCommand> getSupportedUpdateCommand() {
        return UpdateStudentCommand.class;
    }

    @Override
    public PersonDto update(UpdateStudentCommand command) {
        return studentService.update(command);
    }
}
