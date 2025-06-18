package com.drv.filestorage.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Clase para manejar errores en la validación del JWT
 */
public class InvalidJwtException extends AuthenticationException {
    public InvalidJwtException(String message) {
        super(message);
    }
}
