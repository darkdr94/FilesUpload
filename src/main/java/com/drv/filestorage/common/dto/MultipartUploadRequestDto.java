package com.drv.filestorage.common.dto;

import com.drv.filestorage.validation.ValidMimeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidMimeType
public class MultipartUploadRequestDto {

    @NotNull(message = "El nombre del archivo es obligatorio")
    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    @Size(min = 3, max = 200, message = "El nombre del archivo debe tener entre 3 y 200 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El nombre del archivo solo puede contener letras, números, guiones, puntos y guiones bajos"
    )
    private String filename;

    @NotNull(message = "El tamaño del archivo es obligatorio")
    @Positive(message = "El tamaño del archivo debe ser mayor que cero")
    @Min(value = 5 * 1024 * 1024, message = "El tamaño mínimo del archivo es 5 MB")
    @Max(value = 100L * 1024 * 1024 * 1024, message = "El tamaño máximo del archivo es 100 GB")
    private Long fileSizeBytes;

    @NotNull(message = "El tipo de contenido del archivo es obligatorio")
    @NotBlank(message = "El tipo de contenido del archivo no puede estar vacío")
    @Pattern(
            regexp = "^[a-zA-Z0-9.+\\-]+/[a-zA-Z0-9.+\\-]+$",
            message = "El tipo de contenido del archivo no es válido"
    )
    private String contentType;

}
