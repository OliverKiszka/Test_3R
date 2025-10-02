package pl.kurs.test3r.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.dto.EmployeeDto;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.person.PersonType;
import pl.kurs.test3r.models.position.PositionHistory;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmployeeDtoMapper {

    private final ModelMapper modelMapper;

    public EmployeeDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EmployeeDto map(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
        dto.setType(PersonType.EMPLOYEE.name());
        dto.setPositionCount(employee.getPositions() == null ? 0 : employee.getPositions().size());
        dto.setProfessionCount(calculateProfessionCount(employee));
        return dto;
    }

    private int calculateProfessionCount(Employee employee){
        if (employee.getPositions() == null){
            return 0;
        }
        Set<String> professionNames = employee.getPositions().stream()
                .map(PositionHistory::getPositionName)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return professionNames.size();
    }

}
