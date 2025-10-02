package pl.kurs.test3r.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ImportConcurrencyException extends RuntimeException{

    public ImportConcurrencyException(int maxConcurrentImports){
        super("Maximum number of concurrentimports reached (" + maxConcurrentImports + ")");
    }
}
