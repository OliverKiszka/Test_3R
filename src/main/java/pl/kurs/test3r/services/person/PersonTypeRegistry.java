package pl.kurs.test3r.services.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.kurs.test3r.models.person.Person;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PersonTypeRegistry {

    private final Map<String, PersonTypeModule<?, ?, ?>> modulesByType;
    private final Map<Class<?>, PersonTypeModule<?, ?, ?>> modulesByEntityClass;

    public PersonTypeRegistry(ObjectMapper objectMapper, Collection<PersonTypeModule<?, ?, ?>> modules) {
        this.modulesByType = modules.stream()
                .collect(Collectors.toUnmodifiableMap(module -> module.getType().toUpperCase(Locale.ROOT), Function.identity()));
        this.modulesByEntityClass = modules.stream()
                .collect(Collectors.toUnmodifiableMap(module -> module.getPersonClass(), Function.identity()));

        modules.forEach(module -> {
            objectMapper.registerSubtypes(
                    new NamedType(module.getCreateCommandClass(), module.getType()),
                    new NamedType(module.getUpdateCommandClass(), module.getType())
            );
        });
    }

    public PersonTypeModule<?, ?, ?> getByType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("Person type must be provided");
        }
        PersonTypeModule<?, ?, ?> module = modulesByType.get(type.toUpperCase(Locale.ROOT));
        if (module == null) {
            throw new IllegalArgumentException("Unsupported person type: " + type);
        }
        return module;
    }

    public PersonTypeModule<?, ?, ?> getByEntity(Person person) {
        Objects.requireNonNull(person, "Person must not be null");
        PersonTypeModule<?, ?, ?> module = modulesByEntityClass.get(person.getClass());
        if (module == null) {
            throw new IllegalArgumentException("No module registered for entity " + person.getClass());
        }
        return module;
    }

    public Collection<PersonTypeModule<?, ?, ?>> getModules() {
        return modulesByType.values();
    }
}