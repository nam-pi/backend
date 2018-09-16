package eu.nampi.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistingException extends RuntimeException {

    private static final long serialVersionUID = -5739825087133348519L;

}