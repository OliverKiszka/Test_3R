package pl.kurs.test3r.services.person;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import pl.kurs.test3r.commands.CreateRetireeCommand;
import pl.kurs.test3r.commands.UpdateRetireeCommand;
import pl.kurs.test3r.dto.PersonDto;
import pl.kurs.test3r.dto.PersonSearchCriteria;
import pl.kurs.test3r.dto.RetireeDto;
import pl.kurs.test3r.models.person.Person;
import pl.kurs.test3r.models.person.Retiree;
import pl.kurs.test3r.services.RetireeService;
import pl.kurs.test3r.services.imports.PersonCsvRow;

@Component
public class RetireePersonTypeModule extends AbstractPersonTypeModule<CreateRetireeCommand, UpdateRetireeCommand, Retiree> {

    public static final String TYPE = "RETIREE";

    private final RetireeService retireeService;
    private final ModelMapper modelMapper;

    public RetireePersonTypeModule(RetireeService retireeService, ModelMapper modelMapper) {
        this.retireeService = retireeService;
        this.modelMapper = modelMapper;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<CreateRetireeCommand> getCreateCommandClass() {
        return CreateRetireeCommand.class;
    }

    @Override
    public Class<UpdateRetireeCommand> getUpdateCommandClass() {
        return UpdateRetireeCommand.class;
    }

    @Override
    public Class<Retiree> getPersonClass() {
        return Retiree.class;
    }

    @Override
    public PersonDto create(CreateRetireeCommand command) {
        RetireeDto dto = retireeService.create(command);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public PersonDto update(UpdateRetireeCommand command) {
        RetireeDto dto = retireeService.update(command);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public PersonDto map(Retiree person) {
        RetireeDto dto = modelMapper.map(person, RetireeDto.class);
        dto.setType(TYPE);
        return dto;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteria criteria) {
        Specification<Person> specification = null;
        if (!hasAny(criteria,
                criteria.getPensionAmountFrom(),
                criteria.getPensionAmountTo(),
                criteria.getYearsWorkedFrom(),
                criteria.getYearsWorkedTo())) {
            return specification;
        }
        specification = combine(specification, typeSpecification());
        specification = combine(specification, rangeNumber("pensionAmount", criteria.getPensionAmountFrom(), criteria.getPensionAmountTo()));
        specification = combine(specification, rangeInteger("yearsWorked", criteria.getYearsWorkedFrom(), criteria.getYearsWorkedTo()));
        return specification;
    }

    @Override
    public Retiree createFromCsv(PersonCsvRow row) {
        Retiree retiree = new Retiree();
        applyBaseAttributes(retiree, row);
        retiree.setPensionAmount(row.requiredDouble("pensionamount"));
        retiree.setYearsWorked(row.requiredInteger("yearsworked"));
        return retiree;
    }
}