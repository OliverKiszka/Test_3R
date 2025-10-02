package pl.kurs.test3r.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.kurs.test3r.commands.CreateRetireeCommand;
import pl.kurs.test3r.commands.UpdateRetireeCommand;
import pl.kurs.test3r.dto.RetireeDto;
import pl.kurs.test3r.models.person.Retiree;
import pl.kurs.test3r.services.crud.RetireeCrudService;

@Service
@Transactional
public class RetireeService {

    private final RetireeCrudService retireeCrudService;
    private final ModelMapper mapper;

    public RetireeService(RetireeCrudService retireeCrudService, ModelMapper mapper) {
        this.retireeCrudService = retireeCrudService;
        this.mapper = mapper;
    }

    public RetireeDto create(CreateRetireeCommand createRetireeCommand){
        Retiree retiree = mapper.map(createRetireeCommand, Retiree.class);
        Retiree savedRetiree = retireeCrudService.add(retiree);
        return mapper.map(savedRetiree, RetireeDto.class);
    }
    public RetireeDto update(UpdateRetireeCommand updateRetireeCommand){
        Retiree retiree = mapper.map(updateRetireeCommand, Retiree.class);
        retiree.setId(updateRetireeCommand.getId());
        retiree.setVersion(updateRetireeCommand.getVersion());
        Retiree updatedRetiree = retireeCrudService.edit(retiree);
        return mapper.map(updatedRetiree, RetireeDto.class);
    }


}
