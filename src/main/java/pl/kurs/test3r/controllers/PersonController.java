package pl.kurs.test3r.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.test3r.commands.*;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.EmployeeService;
import pl.kurs.test3r.services.RetireeService;
import pl.kurs.test3r.services.StudentService;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final StudentService studentService;
    private final EmployeeService employeeService;
    private final RetireeService retireeService;

    public PersonController(StudentService studentService, EmployeeService employeeService, RetireeService retireeService) {
        this.studentService = studentService;
        this.employeeService = employeeService;
        this.retireeService = retireeService;
    }

    @PostMapping
    private ResponseEntity<PersonDto> create(@RequestBody @Valid CreatePersonCommand createPersonCommand){
        PersonDto result;
        switch (createPersonCommand.getType().toUpperCase()){
            case "STUDENT" -> result = studentService.create((CreateStudentCommand) createPersonCommand);
            case "EMPLOYEE" -> result = employeeService.create((CreateEmployeeCommand)  createPersonCommand);
            case "RETIREE" -> result = retireeService.create((CreateRetireeCommand)  createPersonCommand);
            default -> throw new IllegalArgumentException("Unknown person type: " + createPersonCommand.getType());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @PutMapping
    public ResponseEntity<PersonDto> update(@RequestBody @Valid UpdatePersonCommand updatePersonCommand){
        PersonDto result;

        if (updatePersonCommand instanceof UpdateStudentCommand studentCommand){
            result = studentService.update(studentCommand);
        } else if (updatePersonCommand instanceof UpdateEmployeeCommand employeeCommand){
            result = employeeService.update(employeeCommand);
        } else if (updatePersonCommand instanceof UpdateRetireeCommand retireeCommand){
            result = retireeService.update(retireeCommand);
        } else {
            throw new IllegalArgumentException("Unknown update command type");
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
