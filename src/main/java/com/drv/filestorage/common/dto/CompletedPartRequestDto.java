package com.drv.filestorage.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletedPartRequestDto {

    @Min(value = 1, message = "El número de parte debe ser mayor o igual a 1")
    @Max(value = 10000, message = "El número de parte no puede ser mayor a 10.000")
    private int partNumber;

    @NotNull(message = "El eTag es obligatorio")
    @NotBlank(message = "El eTag no puede estar vacío")
    @Size(min = 32, max = 100, message = "El eTag debe tener entre 32 y 100 caracteres")
    @JsonProperty("eTag")
    private String eTag;
}