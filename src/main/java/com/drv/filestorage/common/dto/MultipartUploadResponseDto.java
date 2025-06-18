package com.drv.filestorage.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta con información del multipart upload iniciado")
public class MultipartUploadResponseDto {

    @Schema(description = "Ruta (key) del archivo en S3", example = "user123/2025/06/archivo.jpg")
    private String key;

    @Schema(description = "Identificador del upload multipart en S3", example = "VXBsb2FkSUQxMjM0NTY3ODkw")
    private String uploadId;

    @Schema(description = "Listado de URLs por cada parte a subir", example = "[{ partNumber: 1, url: \"https://s3...\" }]")
    private List<PartInfoResponseDto> urls;
}
