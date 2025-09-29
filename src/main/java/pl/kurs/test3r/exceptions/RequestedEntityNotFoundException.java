package pl.kurs.test3r.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestedEntityNotFoundException extends EntityNotFoundException {

    private Class<?> entityType;

    public RequestedEntityNotFoundException(String message, Class<?> entityType) {
        super(message);
        this.entityType = entityType;
    }

    public Class<?> getEntityType() {
        return entityType;
    }
}
