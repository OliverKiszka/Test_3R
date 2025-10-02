package pl.kurs.test3r.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kurs.test3r.commands.CreatePersonCommand;
import pl.kurs.test3r.commands.UpdatePersonCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.services.PersonQueryService;
import pl.kurs.test3r.services.PersonService;


@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;
    private final PersonQueryService personQueryService;

    public PersonController(PersonService personService, PersonQueryService personQueryService) {
        this.personService = personService;
        this.personQueryService = personQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public Page<PersonDto> search(PersonSearchCriteria criteria, Pageable pageable){
        return personQueryService.search(criteria, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonDto> create(@RequestBody @Valid CreatePersonCommand createPersonCommand){
        PersonDto result = personService.create(createPersonCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonDto> update(@RequestBody @Valid UpdatePersonCommand updatePersonCommand){
        PersonDto result = personService.update(updatePersonCommand);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
