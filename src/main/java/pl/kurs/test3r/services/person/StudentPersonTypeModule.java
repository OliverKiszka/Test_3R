package pl.kurs.test3r.services.person;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.dto.StudentDto;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.models.person.Student;
import pl.kurs.test3r.services.StudentService;
import pl.kurs.test3r.services.imports.PersonCsvRow;

@Component
public class StudentPersonTypeModule extends AbstractPersonTypeModule<CreateStudentCommand, UpdateStudentCommand, Student> {

    public static final String TYPE = "STUDENT";

    private final StudentService studentService;
    private final ModelMapper modelMapper;

    public StudentPersonTypeModule(StudentService studentService, ModelMapper modelMapper) {
        this.studentService = studentService;
        this.modelMapper = modelMapper;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<CreateStudentCommand> getCreateCommandClass() {
        return CreateStudentCommand.class;
    }

    @Override
    public Class<UpdateStudentCommand> getUpdateCommandClass() {
        return UpdateStudentCommand.class;
    }

    @Override
    public Class<Student> getPersonClass() {
        return Student.class;
    }

    @Override
    public PersonDto create(CreateStudentCommand command) {
        StudentDto dto = studentService.create(command);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public PersonDto update(UpdateStudentCommand command) {
        StudentDto dto = studentService.update(command);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public PersonDto map(Student person) {
        StudentDto dto = modelMapper.map(person, StudentDto.class);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (!hasAny(criteria,
                criteria.getUniversity(),
                criteria.getFieldOfStudy(),
                criteria.getStudyYearFrom(),
                criteria.getStudyYearTo(),
                criteria.getScholarshipFrom(),
                criteria.getScholarshipTo())) {
            return specification;
        }
        specification = combine(specification, typeSpecification());
        if (StringUtils.hasText(criteria.getUniversity())) {
            specification = combine(specification, treatLikeIgnoreCase("university", criteria.getUniversity()));
        }
        if (StringUtils.hasText(criteria.getFieldOfStudy())) {
            specification = combine(specification, treatLikeIgnoreCase("fieldOfStudy", criteria.getFieldOfStudy()));
        }
        specification = combine(specification, rangeInteger("studyYear", criteria.getStudyYearFrom(), criteria.getStudyYearTo()));
        specification = combine(specification, rangeNumber("scholarship", criteria.getScholarshipFrom(), criteria.getScholarshipTo()));
        return specification;
    }

    @Override
    public Student createFromCsv(PersonCsvRow row) {
        Student student = new Student();
        applyBaseAttributes(student, row);
        student.setUniversity(row.required("university"));
        student.setStudyYear(row.requiredInteger("studyyear"));
        student.setFieldOfStudy(row.required("fieldofstudy"));
        student.setScholarship(row.requiredDouble("scholarship"));
        return student;
    }
}