package com.drv.filestorage.controller;

import com.drv.filestorage.common.GenericResponse;
import com.drv.filestorage.common.dto.CompleteUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadResponseDto;
import com.drv.filestorage.service.FilestorageService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Carga de Archivos", description = "Operaciones para manejar cargas multipart de archivos")
public class FilesUploadController {

    @Autowired
    private FilestorageService filestorageService;

    private static final String ERROR_MSG_VAL = "Errores de validación";

    @Hidden
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @Hidden
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Info del servicio");
    }

    @Operation(summary = "Generar URLs prefirmadas para carga multipart",
            description = "Inicia un proceso de carga multipart en S3, validando el nombre, tipo y tamaño del archivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URLs generadas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MultipartUploadResponseDto.class),
                            examples = @ExampleObject(
                                    summary = "Ejemplo de respuesta exitosa",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"message\": \"Multipart upload iniciado correctamente para el archivo test.png\",\n" +
                                            "  \"data\": {\n" +
                                            "    \"key\": \"usuario/2025/06/uuid_test_test.png\",\n" +
                                            "    \"uploadId\": \"EjemploUploadId\",\n" +
                                            "    \"urls\": [\n" +
                                            "      { \"partNumber\": 1, \"url\": \"https://s3...1\" },\n" +
                                            "      { \"partNumber\": 2, \"url\": \"https://s3...2\" }\n" +
                                            "    ]\n" +
                                            "  }\n" +
                                            "}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Errores de validación",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"success\": false,\n" +
                            "    \"data\": null,\n" +
                            "    \"errors\": {\n" +
                            "        \"filename\": \"El nombre del archivo solo puede contener letras, números, guiones, puntos y guiones bajos\"\n" +
                            "    },\n" +
                            "    \"message\": \"Errores de validación\"\n" +
                            "}"))),
            @ApiResponse(responseCode = "401", description = "Error de autenticación",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"status\": 401,\n" +
                            "    \"error\": \"Unauthorized\",\n" +
                            "    \"message\": \"El encabezado de autorizacion es invalido o no esta presente.\",\n" +
                            "    \"path\": \"/files-upload/generate-multipart-urls\"\n" +
                            "}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"error\": \"INTERNAL_SERVER_ERROR\",\n" +
                            "    \"message\": \"Ocurrio un error inesperado\",\n" +
                            "    \"status\": 500\n" +
                            "}")))
    })
    @PostMapping("files-upload/generate-multipart-urls")
    public ResponseEntity<GenericResponse<MultipartUploadResponseDto>> generatePresignedUrls(@Valid @RequestBody MultipartUploadRequestDto request,
                                                                                             BindingResult bindingResult) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Iniciando generacion de url multiparts: usuario: {} nombre: {} contentType: {} bytes: {}"
                , username, request.getFilename(), request.getContentType(), request.getFileSizeBytes());

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            log.info("Errores de validación en los datos de entrada");
            return ResponseEntity.badRequest().body(
                    GenericResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: createMultipartUpload");
        MultipartUploadResponseDto result = filestorageService.createMultipartUpload(request);
        return ResponseEntity.ok(GenericResponse.success(result,
                String.format("Multipart upload iniciado correctamente para el archivo %s", result.getKey())));
    }

    @Operation(
            summary = "Completa el proceso de carga multipart",
            description = "Indica a S3 que todas las partes han sido cargadas y finaliza el proceso de subida"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carga completada exitosamente",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"success\": true,\n" +
                            "    \"data\": null,\n" +
                            "    \"errors\": null,\n" +
                            "    \"message\": \"Subida completada exitosamente\"\n" +
                            "}"))),
            @ApiResponse(responseCode = "400", description = "Errores de validación",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"success\": false,\n" +
                            "    \"data\": null,\n" +
                            "    \"errors\": {\n" +
                            "        \"key\": \"El nombre del archivo (key) no puede estar vacío\"\n" +
                            "    },\n" +
                            "    \"message\": \"Errores de validación\"\n" +
                            "}"))),
            @ApiResponse(responseCode = "401", description = "Error de autenticación",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"status\": 401,\n" +
                            "    \"error\": \"Unauthorized\",\n" +
                            "    \"message\": \"El encabezado de autorizacion es invalido o no esta presente.\",\n" +
                            "    \"path\": \"/files-upload/complete-multiparts-upload\"\n" +
                            "}"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = "{\n" +
                            "    \"error\": \"INTERNAL_SERVER_ERROR\",\n" +
                            "    \"message\": \"Ocurrio un error inesperado\",\n" +
                            "    \"status\": 500\n" +
                            "}")))
    })
    @PostMapping("files-upload/complete-multiparts-upload")
    public ResponseEntity<GenericResponse<Void>> completeUpload(@Valid @RequestBody CompleteUploadRequestDto request,
                                                                BindingResult bindingResult) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Iniciando proceso para completar carga del archivo: usuario: {} key: {} uploadId: {}"
                , username, request.getKey(), request.getUploadId());
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            log.info("Errores de validacion en los datos de entrada");
            return ResponseEntity.badRequest().body(
                    GenericResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: completeMultipartUpload");
        filestorageService.completeMultipartUpload(request);
        log.info("Finalizo proceso para completar carga del archivo: usuario: {} key: {} uploadId: {}"
                , username, request.getKey(), request.getUploadId());
        return ResponseEntity.ok(GenericResponse.success(null, "Subida completada exitosamente"));
    }

}
