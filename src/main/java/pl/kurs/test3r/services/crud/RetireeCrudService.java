package pl.kurs.test3r.services.crud;

import org.springframework.stereotype.Service;
import pl.kurs.test3r.models.person.Retiree;
import pl.kurs.test3r.repositories.RetireeRepository;
@Service
public class RetireeCrudService extends GenericCrudService<Retiree, Long, RetireeRepository> {
    public RetireeCrudService(RetireeRepository repository) {
        super(repository);
    }
}
