package com.drv.filestorage.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Clase que encapsula las respuestas de los servicios
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<T> {
    private boolean success;
    private T data;
    private Map<String, String> errors;
    private String message;

    public static <T> GenericResponse<T> success(T data) {
        return new GenericResponse<>(true, data, null, null);
    }

    public static <T> GenericResponse<T> success(T data, String message) {
        return new GenericResponse<>(true, data, null, message);
    }

    public static <T> GenericResponse<T> error(Map<String, String> errors, String message) {
        return new GenericResponse<>(false, null, errors, message);
    }
}
