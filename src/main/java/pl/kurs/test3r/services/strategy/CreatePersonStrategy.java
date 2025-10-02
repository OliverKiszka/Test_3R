package pl.kurs.test3r.services.strategy;

import pl.kurs.test3r.commands.CreatePersonCommand;
import pl.kurs.test3r.dto.PersonDto;

public interface CreatePersonStrategy<C extends CreatePersonCommand> {
    Class<C> getSupportedCreateCommand();

    PersonDto create(C command);
}
