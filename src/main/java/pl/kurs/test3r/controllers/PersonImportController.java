package pl.kurs.test3r.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.test3r.dto.ImportJobDto;
import pl.kurs.test3r.services.imports.PersonCsvImportService;

@RestController
@RequestMapping("/api/imports/persons")
public class PersonImportController {

    private final PersonCsvImportService personCsvImportService;

    public PersonImportController(PersonCsvImportService personCsvImportService) {
        this.personCsvImportService = personCsvImportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IMPORTER')")
    public ResponseEntity<ImportJobDto> upload(@RequestParam("file") MultipartFile file){
        ImportJobDto dto = personCsvImportService.startImport(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'Importer')")
    public ResponseEntity<ImportJobDto> status(@PathVariable Long id){
        return ResponseEntity.ok(personCsvImportService.getStatus(id));
    }

}
