package pl.kurs.test3r.services.person;

import org.springframework.data.jpa.domain.Specification;
import pl.kurs.test3r.commands.CreatePersonCommand;
import pl.kurs.test3r.commands.UpdatePersonCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.services.imports.PersonCsvRow;

public interface PersonTypeModule<C extends CreatePersonCommand, U extends UpdatePersonCommand, P extends Person> {

    String getType();

    Class<C> getCreateCommandClass();

    Class<U> getUpdateCommandClass();

    Class<P> getPersonClass();

    PersonDto create(C command);

    PersonDto update(U command);

    PersonDto map(P person);

    default PersonDto createUsingBase(CreatePersonCommand command) {
        return create(getCreateCommandClass().cast(command));
    }

    default PersonDto updateUsingBase(UpdatePersonCommand command) {
        return update(getUpdateCommandClass().cast(command));
    }

    default PersonDto mapPerson(Person person) {
        return map(getPersonClass().cast(person));
    }

    default Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        return null;
    }

    default P createFromCsv(PersonCsvRow row) {
        throw new UnsupportedOperationException("CSV import not supported for type " + getType());
    }
}