package pl.kurs.test3r.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalEntityStateException extends IllegalStateException {
    public IllegalEntityStateException(String message) {
        super(message);
    }
}
