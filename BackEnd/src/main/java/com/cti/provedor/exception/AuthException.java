package com.cti.provedor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {

    public AuthException() {
        super("E-mail ou senha invalidos");
    }

    public AuthException(String message) {
        super(message);
    }
}
