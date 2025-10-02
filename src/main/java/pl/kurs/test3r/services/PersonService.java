package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.*;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.services.strategy.CreatePersonStrategy;
import pl.kurs.test3r.services.strategy.UpdatePersonStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonService {

    private final Map<Class<? extends CreatePersonCommand>, CreatePersonStrategy<? extends CreatePersonCommand>> createStrategies;
    private final Map<Class<? extends UpdatePersonCommand>, UpdatePersonStrategy<? extends UpdatePersonCommand>> updateStrategies;

    public PersonService(List<CreatePersonStrategy<? extends  CreatePersonCommand>> createStrategies,
                         List<UpdatePersonStrategy<? extends UpdatePersonCommand>> updateStrategies) {

        this.createStrategies = createStrategies.stream()
                .collect(Collectors.toMap(CreatePersonStrategy::getSupportedCreateCommand, Function.identity()));
        this.updateStrategies = updateStrategies.stream()
                .collect(Collectors.toMap(UpdatePersonStrategy::getSupportedUpdateCommand, Function.identity()));
    }

    public PersonDto create(CreatePersonCommand createPersonCommand){
        CreatePersonStrategy<CreatePersonCommand> strategy = resolveCreateStrategy(createPersonCommand);
        return strategy.create(createPersonCommand);
    }
    public PersonDto update(UpdatePersonCommand updatePersonCommand){
        UpdatePersonStrategy<UpdatePersonCommand> strategy = resolveUpdateStrategy(updatePersonCommand);
        return strategy.update(updatePersonCommand);
    }

    @SuppressWarnings("unchecked")
    private <C extends CreatePersonCommand> CreatePersonStrategy<C> resolveCreateStrategy(C command){
        CreatePersonStrategy<? extends CreatePersonCommand> strategy = createStrategies.get(command.getClass());
        if (strategy == null){
            throw new IllegalArgumentException("Unsupported person type: " + command.getClass().getSimpleName());
        }
        return (CreatePersonStrategy<C>) strategy;
    }

    @SuppressWarnings("unchecked")
    private <U extends UpdatePersonCommand> UpdatePersonStrategy<U> resolveUpdateStrategy(U command) {
        UpdatePersonStrategy<? extends UpdatePersonCommand> strategy = updateStrategies.get(command.getClass());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported person type: " + command.getClass().getSimpleName());
        }
        return (UpdatePersonStrategy<U>) strategy;
    }
}
