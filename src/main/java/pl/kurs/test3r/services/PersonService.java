package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.*;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.person.PersonTypeModule;
import pl.kurs.test3r.services.person.PersonTypeRegistry;

@Service
@Transactional
public class PersonService {

    private final PersonTypeRegistry personTypeRegistry;

    public PersonService(PersonTypeRegistry personTypeRegistry) {
        this.personTypeRegistry = personTypeRegistry;
    }

    public PersonDto create(CreatePersonCommand createPersonCommand) {
        PersonTypeModule<?, ?, ?> module = personTypeRegistry.getByType(createPersonCommand.getType());
        return module.createUsingBase(createPersonCommand);
    }

    public PersonDto update(UpdatePersonCommand updatePersonCommand) {
        PersonTypeModule<?, ?, ?> module = personTypeRegistry.getByType(updatePersonCommand.getType());
        return module.updateUsingBase(updatePersonCommand);
    }


}
