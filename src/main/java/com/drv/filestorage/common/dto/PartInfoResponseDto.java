package com.drv.filestorage.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Información de cada parte para el upload multipart")
public class PartInfoResponseDto {

    @Schema(description = "Número de la parte", example = "1")
    private int partNumber;

    @Schema(description = "URL prefirmada para subir la parte", example = "https://bucket.s3.amazonaws.com/...")
    private String presignedUrl;
}
