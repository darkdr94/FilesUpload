package com.drv.filestorage.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Petición para completar un proceso de carga multipart en S3")
public class CompleteUploadRequestDto {

    @Schema(
            description = "Ruta (key) completa del archivo en S3",
            example = "user123/2025/06/archivo.jpg"
    )
    @NotNull(message = "El nombre del archivo (key) es obligatorio")
    @NotBlank(message = "El nombre del archivo (key) no puede estar vacío")
    @Size(min = 3, max = 1024, message = "El nombre del archivo (key) debe tener entre 3 y 1024 caracteres")
    private String key;

    @Schema(
            description = "Identificador único del upload multipart generado por S3",
            example = "W1tZb3VHaWRWZndzZWc3LUVRZ21jc3guLi4="
    )
    @NotNull(message = "El uploadId es obligatorio")
    @NotBlank(message = "El uploadId es obligatorio")
    @Size(min = 5, max = 1024, message = "El uploadId debe tener entre 5 y 1024 caracteres")
    private String uploadId;

    @Schema(
            description = "Lista de partes completadas con sus números y ETags",
            example = """
        [
            { "partNumber": 1, "eTag": "abc123etaghash1" },
            { "partNumber": 2, "eTag": "abc123etaghash2" }
        ]
        """
    )
    @NotNull(message = "La lista de partes no puede ser nula")
    @NotEmpty(message = "Debe enviar al menos una parte para completar el upload")
    @Size(max = 10000, message = "No se pueden enviar más de 10.000 partes")
    @Valid
    private List<CompletedPartRequestDto> parts;

}
