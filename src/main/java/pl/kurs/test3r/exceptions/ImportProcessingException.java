package pl.kurs.test3r.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImportProcessingException extends RuntimeException{

    public ImportProcessingException(String message) {
        super(message);
    }

    public ImportProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
