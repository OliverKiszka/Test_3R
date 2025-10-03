package pl.kurs.test3r.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.kurs.test3r.commands.CreatePositionCommand;
import pl.kurs.test3r.commands.UpdatePositionCommand;
import pl.kurs.test3r.dto.PositionHistoryDto;
import pl.kurs.test3r.services.PositionService;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}/positions")
public class EmployeePositionController {

    private final PositionService positionService;

    public EmployeePositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<PositionHistoryDto> getPositions(@PathVariable Long employeeId) {
        return positionService.getPositions(employeeId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PositionHistoryDto> createPosition(@PathVariable Long employeeId, @RequestBody @Valid CreatePositionCommand command) {
        PositionHistoryDto dto = positionService.createPosition(employeeId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{positionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public PositionHistoryDto updatePosition(@PathVariable Long employeeId, @PathVariable Long positionId, @RequestBody @Valid UpdatePositionCommand command) {
        return positionService.updatePosition(employeeId, positionId, command);
    }


}
