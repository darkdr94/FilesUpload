package com.drv.filestorage.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta JWT generada al autenticar el usuario")
public class JwtResponseDto {
    @Schema(description = "Token JWT generado", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String token;
}
