package pl.kurs.test3r.services.strategy;

import org.springframework.stereotype.Component;
import pl.kurs.test3r.commands.CreateRetireeCommand;
import pl.kurs.test3r.commands.UpdateRetireeCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.RetireeService;
@Component
public class RetireePersonStrategy implements CreatePersonStrategy<CreateRetireeCommand>, UpdatePersonStrategy<UpdateRetireeCommand> {


    private final RetireeService retireeService;

    public RetireePersonStrategy(RetireeService retireeService) {
        this.retireeService = retireeService;
    }

    @Override
    public Class<CreateRetireeCommand> getSupportedCreateCommand() {
        return CreateRetireeCommand.class;
    }

    @Override
    public PersonDto create(CreateRetireeCommand command) {
        return retireeService.create(command);
    }

    @Override
    public Class<UpdateRetireeCommand> getSupportedUpdateCommand() {
        return UpdateRetireeCommand.class;
    }

    @Override
    public PersonDto update(UpdateRetireeCommand command) {
        return retireeService.update(command);
    }
}
