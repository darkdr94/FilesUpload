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
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private Map<String, String> errors;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, null, message);
    }

    public static <T> ApiResponse<T> error(Map<String, String> errors, String message) {
        return new ApiResponse<>(false, null, errors, message);
    }
}
