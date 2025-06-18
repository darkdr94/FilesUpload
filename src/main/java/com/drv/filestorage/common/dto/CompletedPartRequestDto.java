package com.drv.filestorage.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Representa una parte individual de una carga multipart completada")
public class CompletedPartRequestDto {

    @Min(value = 1, message = "El número de parte debe ser mayor o igual a 1")
    @Max(value = 10000, message = "El número de parte no puede ser mayor a 10.000")
    @Schema(description = "Número de parte de la carga multipart", example = "1", minimum = "1", maximum = "10000")
    private int partNumber;

    @Schema(
            name = "eTag",
            description = "ETag devuelto por S3 al subir esta parte",
            example = "\"abc123etaghashabcdef1234567890\""
    )
    @NotNull(message = "El eTag es obligatorio")
    @NotBlank(message = "El eTag no puede estar vacío")
    @Size(min = 32, max = 100, message = "El eTag debe tener entre 32 y 100 caracteres")
    @JsonProperty("eTag")
    private String eTag;
}