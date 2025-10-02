package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.CreatePositionCommand;
import pl.kurs.test3r.commands.UpdatePositionCommand;
import pl.kurs.test3r.dto.PositionHistoryDto;
import pl.kurs.test3r.exceptions.IllegalEntityStateException;
import pl.kurs.test3r.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.position.PositionHistory;
import pl.kurs.test3r.repositories.EmployeeRepository;
import pl.kurs.test3r.repositories.PositionRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;

    public PositionService(PositionRepository positionRepository, EmployeeRepository employeeRepository) {
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<PositionHistoryDto> getPositions(Long employeeId) {
        Employee employee = employeeRepository.findByIdWithPositions(employeeId)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Employee with id " + employeeId + " not found", Employee.class));
        return employee.getPositions().stream()
                .sorted(Comparator.comparing(PositionHistory::getDateFrom, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(PositionHistory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PositionHistoryDto createPosition(Long employeeId, CreatePositionCommand command) {
        Employee employee = employeeRepository.findByIdWithPositionsForUpdate(employeeId)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Employee with id " + employeeId + " not found", Employee.class));

        PositionHistory newPosition = new PositionHistory();
        newPosition.setPositionName(command.getPositionName());
        newPosition.setSalary(command.getSalary());
        newPosition.setDateFrom(command.getDateFrom());
        newPosition.setDateTo(command.getDateTo());
        newPosition.setEmployee(employee);

        validatePositionRange(newPosition);
        List<PositionHistory> currentPositions = lockAndAttachPositions(employee);
        validateNoOverlaps(newPosition, currentPositions, null);

        PositionHistory saved = positionRepository.save(newPosition);
        employee.getPositions().add(saved);
        refreshEmployeeCurrentState(employee);
        return toDto(saved);
    }

    public PositionHistoryDto updatePosition(Long employeeId, Long positionId, UpdatePositionCommand command) {
        PositionHistory existing = positionRepository.findByIdWithEmployeeForUpdate(positionId)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Position with id " + positionId + " not found", PositionHistory.class));

        Employee employee = existing.getEmployee();
        if (!Objects.equals(employee.getId(), employeeId)) {
            throw new IllegalEntityStateException("Position does not belong to employee " + employeeId);
        }

        existing.setPositionName(command.getPositionName());
        existing.setSalary(command.getSalary());
        existing.setDateFrom(command.getDateFrom());
        existing.setDateTo(command.getDateTo());

        validatePositionRange(existing);
        List<PositionHistory> currentPositions = lockAndAttachPositions(employee);
        validateNoOverlaps(existing, currentPositions, existing.getId());

        refreshEmployeeCurrentState(employee);
        return toDto(existing);
    }


    private PositionHistoryDto toDto(PositionHistory entity) {
        PositionHistoryDto dto = new PositionHistoryDto();
        dto.setId(entity.getId());
        dto.setPositionName(entity.getPositionName());
        dto.setSalary(entity.getSalary());
        dto.setDateFrom(entity.getDateFrom());
        dto.setDateTo(entity.getDateTo());
        return dto;
    }

    private void validatePositionRange(PositionHistory positionHistory) {
        if (positionHistory.getDateFrom() == null) {
            throw new IllegalEntityStateException("Position start date cannot be null");
        }
        if (positionHistory.getDateTo() != null && positionHistory.getDateTo().isBefore(positionHistory.getDateFrom())) {
            throw new IllegalEntityStateException("Position end date cannot be before start date");
        }
    }

    private void validateNoOverlaps(PositionHistory candidate, List<PositionHistory> currentPositions, Long ignoredId) {
        LocalDate candidateStart = candidate.getDateFrom();
        LocalDate candidateEnd = candidate.getDateTo() == null ? LocalDate.MAX : candidate.getDateTo();
        for (PositionHistory history : currentPositions) {
            if (ignoredId != null && Objects.equals(history.getId(), ignoredId)) {
                continue;
            }
            LocalDate historyStart = history.getDateFrom();
            LocalDate historyEnd = history.getDateTo() == null ? LocalDate.MAX : history.getDateTo();
            if (historyStart == null) {
                continue;
            }
            boolean overlaps = !candidateStart.isAfter(historyEnd) && !historyStart.isAfter(candidateEnd);
            if (overlaps) {
                throw new IllegalEntityStateException("Position history dates overlap for employee " + candidate.getEmployee().getId());
            }
        }
    }

    private List<PositionHistory> lockAndAttachPositions(Employee employee) {
        List<PositionHistory> positions = positionRepository.findByEmployeeIdForUpdate(employee.getId());
        employee.getPositions().clear();
        employee.getPositions().addAll(positions);
        return employee.getPositions();
    }

    private void refreshEmployeeCurrentState(Employee employee) {
        PositionHistory latest = employee.getPositions().stream()
                .max(Comparator.comparing(PositionHistory::getDateFrom, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(PositionHistory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (latest == null) {
            employee.setCurrentPosition(null);
            employee.setCurrentSalary(0.0);
            return;
        }
        employee.setCurrentPosition(latest.getPositionName());
        employee.setCurrentSalary(latest.getSalary());
    }
}
