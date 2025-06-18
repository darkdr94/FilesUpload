package com.drv.filestorage.exception;

import com.drv.filestorage.exception.base.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralizador de manejo de excepciones
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private static final String ERROR ="error";
    private static final String MESSAGE ="message";
    private static final String STATUS ="status";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR, ex.getCode());
        error.put(MESSAGE, ex.getMessage());
        error.put(STATUS, ex.getStatus());
        log.error("API exception caught: {} - {} -", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR, "INTERNAL_SERVER_ERROR");
        error.put(MESSAGE, "Ocurrio un error inesperado");
        error.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON).
                body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBody(HttpMessageNotReadableException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR, "INVALID_REQUEST_BODY");
        error.put(MESSAGE, "El cuerpo de la solicitud es invalido o esta vacio.");
        error.put(STATUS, HttpStatus.BAD_REQUEST.value());
        log.warn("Invalid request body: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
