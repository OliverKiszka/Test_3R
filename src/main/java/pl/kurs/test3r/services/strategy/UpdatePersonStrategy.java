package pl.kurs.test3r.services.strategy;

import pl.kurs.test3r.commands.UpdatePersonCommand;
import pl.kurs.test3r.dto.PersonDto;

public interface UpdatePersonStrategy<U extends UpdatePersonCommand> {

    Class<U> getSupportedUpdateCommand();

    PersonDto update(U command);
}
