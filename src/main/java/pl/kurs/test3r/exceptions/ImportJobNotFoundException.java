package pl.kurs.test3r.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImportJobNotFoundException extends RuntimeException{
    public ImportJobNotFoundException(Long id){
        super("Import job with id " + id + " not found");
    }
}
