package pl.kurs.test3r.services.crud;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.test3r.exceptions.IllegalEntityIdException;
import pl.kurs.test3r.exceptions.IllegalEntityStateException;
import pl.kurs.test3r.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3r.models.Identificationable;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class GenericCrudService<T extends Identificationable, ID extends Number, R extends JpaRepository<T, Long>> implements ICrudService<T> {
    protected R repository;
    protected Class<T> entityType;

    public GenericCrudService(R repository) {
        this.repository = repository;
        this.entityType = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public T add(T entity) {
        if (entity.getId() != null)
            throw new IllegalEntityStateException("ID must be null before persist!");
        return repository.save(entity);
    }

    @Override
    public T edit(T entity) {
        if (entity.getId() == null)
            throw new IllegalEntityStateException("ID cannot be null when editing!");
        if (!repository.existsById(entity.getId()))
            throw new RequestedEntityNotFoundException("Entity with id " + entity.getId() + " not found", entityType);
        return repository.save(entity);
    }

    @Override
    public T get(Long id) {
        if (id == null)
            throw new IllegalEntityIdException("ID cannot be null!");
        return repository.findById(id)
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity with id " + id + " not found", entityType));
    }

    @Override
    public List<T> getAll() {
        return repository.findAll();
    }
}
