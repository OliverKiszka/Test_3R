package pl.kurs.test3r.services;

import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.dto.*;
import pl.kurs.test3r.mappers.EmployeeDtoMapper;
import pl.kurs.test3r.models.person.*;
import pl.kurs.test3r.repositories.PersonRepository;

import java.time.LocalDate;

@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class PersonQueryService {

    private final PersonRepository personRepository;
    private final ModelMapper mapper;
    private final EmployeeDtoMapper employeeDtoMapper;

    public PersonQueryService(PersonRepository personRepository, ModelMapper mapper, EmployeeDtoMapper employeeDtoMapper) {
        this.personRepository = personRepository;
        this.mapper = mapper;
        this.employeeDtoMapper = employeeDtoMapper;
    }

    public Page<PersonDto> search(PersonSearchCriteria criteria, Pageable pageable) {
        Specification<Person> specification = null;

        if (criteria.getType() != null) {
            specification = combine(specification, hasType(criteria.getType()));
        }
        if (StringUtils.hasText(criteria.getFirstName())) {
            specification = combine(specification, likeIgnoreCase("firstName", criteria.getFirstName()));
        }
        if (StringUtils.hasText(criteria.getLastName())) {
            specification = combine(specification, likeIgnoreCase("lastName", criteria.getLastName()));
        }
        if (StringUtils.hasText(criteria.getPesel())) {
            specification = combine(specification, equal("pesel", criteria.getPesel()));
        }
        if (StringUtils.hasText(criteria.getEmail())) {
            specification = combine(specification, likeIgnoreCase("email", criteria.getEmail()));
        }
        specification = combine(specification, range("height", criteria.getHeightFrom(), criteria.getHeightTo()));
        specification = combine(specification, range("weight", criteria.getWeightFrom(), criteria.getWeightTo()));

        specification = combine(specification, employeeSpecification(criteria));
        specification = combine(specification, studentSpecification(criteria));
        specification = combine(specification, retireeSpecification(criteria));

        Page<Person> persons = personRepository.findAll(specification, pageable);
        return persons.map(this::mapToDto);
    }

    private Specification<Person> employeeSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (!StringUtils.hasText(criteria.getCurrentPosition())
                && criteria.getStartDateFrom() == null
                && criteria.getStartDateTo() == null
                && criteria.getSalaryFrom() == null
                && criteria.getSalaryTo() == null
                && criteria.getPositionCountFrom() == null
                && criteria.getPositionCountTo() == null
                && criteria.getProfessionCountFrom() == null
                && criteria.getProfessionCountTo() == null) {
            return specification;
        }

        specification = combine(specification, isEmployee());

        if (StringUtils.hasText(criteria.getCurrentPosition())) {
            specification = combine(specification, (root, query, cb) -> {
                var employee = cb.treat(root, Employee.class);
                return cb.equal(cb.lower(employee.get("currentPosition")), criteria.getCurrentPosition().toLowerCase());
            });
        }
        if (criteria.getStartDateFrom() != null || criteria.getStartDateTo() != null) {
            specification = combine(specification, rangeDate(Employee.class, "startDate", criteria.getStartDateFrom(), criteria.getStartDateTo()));
        }
        if (criteria.getSalaryFrom() != null || criteria.getSalaryTo() != null) {
            specification = combine(specification, rangeNumber(Employee.class, "currentSalary", criteria.getSalaryFrom(), criteria.getSalaryTo()));
        }
        if (criteria.getPositionCountFrom() != null || criteria.getPositionCountTo() != null) {
            specification = combine(specification, positionCountRange(criteria.getPositionCountFrom(), criteria.getPositionCountTo()));
        }
        if (criteria.getProfessionCountFrom() != null || criteria.getProfessionCountTo() != null) {
            specification = combine(specification, professionCountRange(criteria.getProfessionCountFrom(), criteria.getProfessionCountTo()));
        }
        return specification;
    }

    private Specification<Person> studentSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (!StringUtils.hasText(criteria.getUniversity())
                && criteria.getStudyYearFrom() == null
                && criteria.getStudyYearTo() == null
                && !StringUtils.hasText(criteria.getFieldOfStudy())
                && criteria.getScholarshipFrom() == null
                && criteria.getScholarshipTo() == null) {
            return specification;
        }
        specification = combine(specification, isStudent());

        if (StringUtils.hasText(criteria.getUniversity())) {
            specification = combine(specification, (root, query, cb) -> {
                var student = cb.treat(root, Student.class);
                return cb.equal(cb.lower(student.get("university")), criteria.getUniversity().toLowerCase());
            });
        }
        if (StringUtils.hasText(criteria.getFieldOfStudy())) {
            specification = combine(specification, (root, query, cb) -> {
                var student = cb.treat(root, Student.class);
                return cb.equal(cb.lower(student.get("fieldOfStudy")), criteria.getFieldOfStudy().toLowerCase());
            });
        }
        if (criteria.getStudyYearFrom() != null || criteria.getStudyYearTo() != null) {
            specification = combine(specification, rangeInteger(Student.class, "studyYear", criteria.getStudyYearFrom(), criteria.getStudyYearTo()));
        }
        if (criteria.getScholarshipFrom() != null || criteria.getScholarshipTo() != null) {
            specification = combine(specification, rangeNumber(Student.class, "scholarship", criteria.getScholarshipFrom(), criteria.getScholarshipTo()));
        }
        return specification;
    }

    private Specification<Person> retireeSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (criteria.getPensionAmountFrom() == null
                && criteria.getPensionAmountTo() == null
                && criteria.getYearsWorkedFrom() == null
                && criteria.getYearsWorkedTo() == null) {
            return specification;
        }
        specification = combine(specification, isRetiree());

        if (criteria.getPensionAmountFrom() != null || criteria.getPensionAmountTo() != null) {
            specification = combine(specification, rangeNumber(Retiree.class, "pensionAmount", criteria.getPensionAmountFrom(), criteria.getPensionAmountTo()));
        }
        if (criteria.getYearsWorkedFrom() != null || criteria.getYearsWorkedTo() != null) {
            specification = combine(specification, rangeInteger(Retiree.class, "yearsWorked", criteria.getYearsWorkedFrom(), criteria.getYearsWorkedTo()));
        }
        return specification;
    }

    private Specification<Person> combine(Specification<Person> base, Specification<Person> addition) {
        if (addition == null) {
            return base;
        }
        if (base == null) {
            return Specification.where(addition);
        }
        return base.and(addition);
    }

    private Specification<Person> hasType(PersonType type) {
        return (root, query, cb) -> cb.equal(root.type(), type.getEntityClass());
    }

    private Specification<Person> likeIgnoreCase(String attribute, String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), "%" + value.toLowerCase() + "%");
    }

    private Specification<Person> equal(String attribute, String value) {
        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    private Specification<Person> range(String attribute, Double from, Double to) {
        return (root, query, cb) -> {
            var path = root.<Double>get(attribute);
            if (from != null && to != null) {
                return cb.between(path, from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(path, from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(path, to);
            }
            return cb.conjunction();
        };
    }

    private Specification<Person> rangeDate(Class<? extends Person> type, String attribute, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            var treated = cb.treat(root, type);
            var path = treated.<LocalDate>get(attribute);
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), type), cb.between(path, from, to));
            } else if (from != null) {
                return cb.and(cb.equal(root.type(), type), cb.greaterThanOrEqualTo(path, from));
            } else if (to != null) {
                return cb.and(cb.equal(root.type(), type), cb.lessThanOrEqualTo(path, to));
            }
            return cb.conjunction();
        };
    }

    private Specification<Person> rangeNumber(Class<? extends Person> type, String attribute, Double from, Double to) {
        return (root, query, cb) -> {
            var treated = cb.treat(root, type);
            var path = treated.<Double>get(attribute);
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), type), cb.between(path, from, to));
            } else if (from != null) {
                return cb.and(cb.equal(root.type(), type), cb.greaterThanOrEqualTo(path, from));
            } else if (to != null) {
                return cb.and(cb.equal(root.type(), type), cb.lessThanOrEqualTo(path, to));
            }
            return cb.conjunction();
        };
    }

    private Specification<Person> rangeInteger(Class<? extends Person> type, String attribute, Integer from, Integer to) {
        return (root, query, cb) -> {
            var treated = cb.treat(root, type);
            var path = treated.<Integer>get(attribute);
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), type), cb.between(path, from, to));
            } else if (from != null) {
                return cb.and(cb.equal(root.type(), type), cb.greaterThanOrEqualTo(path, from));
            } else if (to != null) {
                return cb.and(cb.equal(root.type(), type), cb.lessThanOrEqualTo(path, to));
            }
            return cb.conjunction();
        };
    }

    private Specification<Person> isEmployee() {
        return (root, query, cb) -> cb.equal(root.type(), Employee.class);
    }

    private Specification<Person> isStudent() {
        return (root, query, cb) -> cb.equal(root.type(), Student.class);
    }

    private Specification<Person> isRetiree() {
        return (root, query, cb) -> cb.equal(root.type(), Retiree.class);
    }

    private Specification<Person> positionCountRange(Integer from, Integer to) {
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
            } else if (from != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.greaterThanOrEqualTo(subquery, from.longValue()));
            } else if (to != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.lessThanOrEqualTo(subquery, to.longValue()));
            }
            return cb.conjunction();
        };
    }

    private Specification<Person> professionCountRange(Integer from, Integer to) {
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
            } else if (from != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.greaterThanOrEqualTo(subquery, from.longValue()));
            } else if (to != null) {
                return cb.and(cb.equal(root.type(), Employee.class), cb.lessThanOrEqualTo(subquery, to.longValue()));
            }
            return cb.conjunction();
        };
    }

    private PersonDto mapToDto(Person person) {
        if (person instanceof Employee employee) {
            return employeeDtoMapper.map(employee);
        } else if (person instanceof Student student) {
            StudentDto dto = mapper.map(student, StudentDto.class);
            dto.setType(PersonType.STUDENT.name());
            return dto;
        } else if (person instanceof Retiree retiree) {
            RetireeDto dto = mapper.map(retiree, RetireeDto.class);
            dto.setType(PersonType.RETIREE.name());
            return dto;
        }
        PersonDto dto = mapper.map(person, PersonDto.class);
        dto.setType(person.getClass().getSimpleName().toUpperCase());
        return dto;
    }

}