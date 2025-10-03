package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.repositories.PersonRepository;
import pl.kurs.test3r.services.person.PersonTypeModule;
import pl.kurs.test3r.services.person.PersonTypeRegistry;

@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class PersonQueryService {

    private final PersonRepository personRepository;
    private final PersonTypeRegistry personTypeRegistry;

    public PersonQueryService(PersonRepository personRepository, PersonTypeRegistry personTypeRegistry) {
        this.personRepository = personRepository;
        this.personTypeRegistry = personTypeRegistry;
    }

    public Page<PersonDto> search(PersonSearchCriteria criteria, Pageable pageable) {
        Specification<Person> specification = buildBaseSpecification(criteria);
        for (PersonTypeModule<?, ?, ?> module : personTypeRegistry.getModules()) {
            specification = combine(specification, module.buildSpecification(criteria));
        }
        Page<Person> persons = personRepository.findAll(specification, pageable);

        return persons.map(person -> personTypeRegistry.getByEntity(person).mapPerson(person));
    }

    private Specification<Person> buildBaseSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (StringUtils.hasText(criteria.getType())) {
            PersonTypeModule<?, ?, ?> module = personTypeRegistry.getByType(criteria.getType());
            specification = combine(specification, (root, query, cb) -> cb.equal(root.type(), module.getPersonClass()));
        }
        if (StringUtils.hasText(criteria.getFirstName())) {
            specification = combine(specification, likeIgnoreCase("firstName", criteria.getFirstName()));
        }
        if (StringUtils.hasText(criteria.getLastName())) {
            specification = combine(specification, likeIgnoreCase("lastName", criteria.getLastName()));
        }
        if (StringUtils.hasText(criteria.getPesel())) {
            specification = combine(specification, likeIgnoreCase("pesel", criteria.getPesel()));
        }
        if (StringUtils.hasText(criteria.getEmail())) {
            specification = combine(specification, likeIgnoreCase("email", criteria.getEmail()));
        }
        specification = combine(specification, range("height", criteria.getHeightFrom(), criteria.getHeightTo()));
        specification = combine(specification, range("weight", criteria.getWeightFrom(), criteria.getWeightTo()));
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


    private Specification<Person> likeIgnoreCase(String attribute, String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), "%" + value.toLowerCase() + "%");
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


}