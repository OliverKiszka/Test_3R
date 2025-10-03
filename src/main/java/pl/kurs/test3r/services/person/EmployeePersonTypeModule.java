package pl.kurs.test3r.services.person;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.UpdateEmployeeCommand;
import pl.kurs.test3r.dto.EmployeeDto;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.mappers.EmployeeDtoMapper;
import pl.kurs.test3r.models.person.Employee;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.services.EmployeeService;
import pl.kurs.test3r.services.imports.PersonCsvRow;

import java.time.LocalDate;

@Component
public class EmployeePersonTypeModule extends AbstractPersonTypeModule<CreateEmployeeCommand, UpdateEmployeeCommand, Employee> {

    public static final String TYPE = "EMPLOYEE";

    private final EmployeeService employeeService;
    private final EmployeeDtoMapper employeeDtoMapper;

    public EmployeePersonTypeModule(EmployeeService employeeService, EmployeeDtoMapper employeeDtoMapper) {
        this.employeeService = employeeService;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<CreateEmployeeCommand> getCreateCommandClass() {
        return CreateEmployeeCommand.class;
    }

    @Override
    public Class<UpdateEmployeeCommand> getUpdateCommandClass() {
        return UpdateEmployeeCommand.class;
    }

    @Override
    public Class<Employee> getPersonClass() {
        return Employee.class;
    }

    @Override
    public PersonDto create(CreateEmployeeCommand command) {
        return employeeService.create(command);
    }

    @Override
    public PersonDto update(UpdateEmployeeCommand command) {
        return employeeService.update(command);
    }

    @Override
    public PersonDto map(Employee person) {
        EmployeeDto dto = employeeDtoMapper.map(person);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (!hasAny(criteria,
                criteria.getCurrentPosition(),
                criteria.getStartDateFrom(),
                criteria.getStartDateTo(),
                criteria.getSalaryFrom(),
                criteria.getSalaryTo(),
                criteria.getPositionCountFrom(),
                criteria.getPositionCountTo(),
                criteria.getProfessionCountFrom(),
                criteria.getProfessionCountTo())) {
            return specification;
        }
        specification = combine(specification, typeSpecification());
        if (StringUtils.hasText(criteria.getCurrentPosition())) {
            specification = combine(specification, treatLikeIgnoreCase("currentPosition", criteria.getCurrentPosition()));
        }
        if (criteria.getStartDateFrom() != null || criteria.getStartDateTo() != null) {
            specification = combine(specification, (root, query, cb) -> {
                var treated = cb.treat(root, Employee.class);
                var path = treated.<LocalDate>get("startDate");
                if (criteria.getStartDateFrom() != null && criteria.getStartDateTo() != null) {
                    return cb.and(cb.equal(root.type(), Employee.class), cb.between(path, criteria.getStartDateFrom(), criteria.getStartDateTo()));
                }
                if (criteria.getStartDateFrom() != null) {
                    return cb.and(cb.equal(root.type(), Employee.class), cb.greaterThanOrEqualTo(path, criteria.getStartDateFrom()));
                }
                return cb.and(cb.equal(root.type(), Employee.class), cb.lessThanOrEqualTo(path, criteria.getStartDateTo()));
            });
        }
        specification = combine(specification, rangeNumber("currentSalary", criteria.getSalaryFrom(), criteria.getSalaryTo()));
        if (criteria.getPositionCountFrom() != null || criteria.getPositionCountTo() != null) {
            specification = combine(specification, positionCountRange(criteria));
        }
        if (criteria.getProfessionCountFrom() != null || criteria.getProfessionCountTo() != null) {
            specification = combine(specification, professionCountRange(criteria));
        }
        return specification;
    }

    private Specification<Person> positionCountRange(PersonSearchCriteria criteria) {
        Integer from = criteria.getPositionCountFrom();
        Integer to = criteria.getPositionCountTo();
        return (root, query, cb) -> {
            query.distinct(true);
            var subquery = query.subquery(Long.class);
            var employeeRoot = subquery.from(Employee.class);
            var positions = employeeRoot.join("positions", JoinType.LEFT);
            subquery.select(cb.count(positions.get("id")));
            subquery.groupBy(employeeRoot.get("id"));
            subquery.where(cb.equal(employeeRoot.get("id"), root.get("id")));
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.between(subquery, from.longValue(), to.longValue()));
            }
            if (from != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.greaterThanOrEqualTo(subquery, from.longValue()));
            }
            return cb.and(cb.equal(root.type(), Employee.class), cb.lessThanOrEqualTo(subquery, to.longValue()));
        };
    }

    private Specification<Person> professionCountRange(PersonSearchCriteria criteria) {
        Integer from = criteria.getProfessionCountFrom();
        Integer to = criteria.getProfessionCountTo();
        return (root, query, cb) -> {
            query.distinct(true);
            var subquery = query.subquery(Long.class);
            var employeeRoot = subquery.from(Employee.class);
            var positions = employeeRoot.join("positions", JoinType.LEFT);
            subquery.select(cb.countDistinct(positions.get("positionName")));
            subquery.groupBy(employeeRoot.get("id"));
            subquery.where(cb.equal(employeeRoot.get("id"), root.get("id")));
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.between(subquery, from.longValue(), to.longValue()));
            }
            if (from != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.greaterThanOrEqualTo(subquery, from.longValue()));
            }
            return cb.and(cb.equal(root.type(), Employee.class), cb.lessThanOrEqualTo(subquery, to.longValue()));
        };
    }

    @Override
    public Employee createFromCsv(PersonCsvRow row) {
        Employee employee = new Employee();
        applyBaseAttributes(employee, row);
        employee.setStartDate(row.requiredDate("startdate"));
        employee.setCurrentPosition(row.required("currentposition"));
        employee.setCurrentSalary(row.requiredDouble("currentsalary"));
        return employee;
    }
}