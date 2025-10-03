package pl.kurs.test3r.services.crud;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pl.kurs.test3r.exceptions.IllegalEntityIdException;
import pl.kurs.test3r.exceptions.IllegalEntityStateException;
import pl.kurs.test3r.exceptions.RequestedEntityNotFoundException;
import pl.kurs.test3r.models.Identificationable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
        T managedEntity = repository.findById(entity.getId())
                .orElseThrow(() -> new RequestedEntityNotFoundException("Entity with id " + entity.getId() + " not found", entityType));

        Long requestedVersion = getVersion(entity);
        Long currentVersion = getVersion(managedEntity);
        if (currentVersion != null || requestedVersion != null) {
            if (requestedVersion == null) {
                throw new IllegalEntityStateException("Version must be provided when editing!");
            }
            if (!requestedVersion.equals(currentVersion)) {
                throw new ObjectOptimisticLockingFailureException(entityType, entity.getId());
            }
        }

        String[] ignoreProperties = Stream.concat(Stream.of("id", "version"), Arrays.stream(getNullPropertyNames(entity)))
                .distinct()
                .toArray(String[]::new);
        BeanUtils.copyProperties(entity, managedEntity, ignoreProperties);
        repository.flush();
        return managedEntity;

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

    private String[] getNullPropertyNames(T source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(propertyName -> !"class".equals(propertyName))
                .filter(propertyName -> src.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    private Long getVersion(T source) {
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        if (!wrapper.isReadableProperty("version")) {
            return null;
        }
        Object value = wrapper.getPropertyValue("version");
        return value == null ? null : (Long) value;
    }

}
