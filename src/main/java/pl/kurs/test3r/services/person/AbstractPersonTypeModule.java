package pl.kurs.test3r.services.person;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.services.imports.PersonCsvRow;

import java.util.Locale;

public abstract class AbstractPersonTypeModule<C extends pl.kurs.test3r.commands.CreatePersonCommand, U extends pl.kurs.test3r.commands.UpdatePersonCommand, P extends Person>
        implements PersonTypeModule<C, U, P> {

    protected Specification<Person> combine(Specification<Person> base, Specification<Person> addition) {
        if (addition == null) {
            return base;
        }
        if (base == null) {
            return Specification.where(addition);
        }
        return base.and(addition);
    }

    protected Specification<Person> typeSpecification() {
        return (root, query, cb) -> cb.equal(root.type(), getPersonClass());
    }

    protected Specification<Person> likeIgnoreCase(String attribute, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String pattern = "%" + value.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), pattern);
    }

    protected Specification<Person> treatLikeIgnoreCase(String attribute, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String pattern = "%" + value.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> {
            var treated = cb.treat(root, getPersonClass());
            return cb.and(cb.equal(root.type(), getPersonClass()), cb.like(cb.lower(treated.get(attribute)), pattern));
        };
    }

    protected Specification<Person> rangeNumber(String attribute, Double from, Double to) {
        if (from == null && to == null) {
            return null;
        }
        return (root, query, cb) -> {
            var treated = cb.treat(root, getPersonClass());
            var path = treated.<Double>get(attribute);
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), getPersonClass()), cb.between(path, from, to));
            }
            if (from != null) {
                return cb.and(cb.equal(root.type(), getPersonClass()), cb.greaterThanOrEqualTo(path, from));
            }
            return cb.and(cb.equal(root.type(), getPersonClass()), cb.lessThanOrEqualTo(path, to));
        };
    }

    protected Specification<Person> rangeInteger(String attribute, Integer from, Integer to) {
        if (from == null && to == null) {
            return null;
        }
        return (root, query, cb) -> {
            var treated = cb.treat(root, getPersonClass());
            var path = treated.<Integer>get(attribute);
            if (from != null && to != null) {
                return cb.and(cb.equal(root.type(), getPersonClass()), cb.between(path, from, to));
            }
            if (from != null) {
                return cb.and(cb.equal(root.type(), getPersonClass()), cb.greaterThanOrEqualTo(path, from));
            }
            return cb.and(cb.equal(root.type(), getPersonClass()), cb.lessThanOrEqualTo(path, to));
        };
    }

    protected boolean hasAny(PersonSearchCriteria criteria, Object... values) {
        for (Object value : values) {
            if (value instanceof String str && StringUtils.hasText(str)) {
                return true;
            }
            if (value != null && !(value instanceof String)) {
                return true;
            }
        }
        return false;
    }

    protected void applyBaseAttributes(P person, PersonCsvRow row) {
        person.setFirstName(row.firstName());
        person.setLastName(row.lastName());
        person.setPesel(row.pesel());
        person.setHeight(row.height());
        person.setWeight(row.weight());
        person.setEmail(row.email());
    }
}