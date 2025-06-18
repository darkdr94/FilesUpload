package com.drv.filestorage.common.dto;

import com.drv.filestorage.validation.ValidMimeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidMimeType
@Schema(description = "Petición para generar URLs prefirmadas para carga multipart de archivos")
public class MultipartUploadRequestDto {

    @Schema(
            description = "Nombre del archivo a subir (sin rutas). Puede incluir letras, números, puntos, guiones y guiones bajos.",
            example = "mi_archivo.jpg"
    )
    @NotNull(message = "El nombre del archivo es obligatorio")
    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    @Size(min = 3, max = 200, message = "El nombre del archivo debe tener entre 3 y 200 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El nombre del archivo solo puede contener letras, números, guiones, puntos y guiones bajos"
    )
    private String filename;

    @Schema(
            description = "Tamaño total del archivo en bytes. Mínimo 5 MB y máximo 100 GB.",
            example = "104857600"
    )
    @NotNull(message = "El tamaño del archivo es obligatorio")
    @Positive(message = "El tamaño del archivo debe ser mayor que cero")
    @Min(value = 5 * 1024 * 1024, message = "El tamaño mínimo del archivo es 5 MB")
    @Max(value = 100L * 1024 * 1024 * 1024, message = "El tamaño máximo del archivo es 100 GB")
    private Long fileSizeBytes;

    @Schema(
            description = "Tipo MIME del archivo (por ejemplo, image/jpeg, application/pdf).",
            example = "application/pdf"
    )
    @NotNull(message = "El tipo de contenido del archivo es obligatorio")
    @NotBlank(message = "El tipo de contenido del archivo no puede estar vacío")
    @Pattern(
            regexp = "^[a-zA-Z0-9.+\\-]+/[a-zA-Z0-9.+\\-]+$",
            message = "El tipo de contenido del archivo no es válido"
    )
    private String contentType;

}
