package com.drv.filestorage.controller;

import com.drv.filestorage.common.GenericResponse;
import com.drv.filestorage.common.SwaggerExamples;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Carga de Archivos", description = "Operaciones para manejar cargas multipart de archivos")
public class FilesUploadController {

    private FilestorageService filestorageService;

    private static final String ERROR_MSG_VAL = "Errores de validación";

    public FilesUploadController(FilestorageService filestorageService) {
        this.filestorageService = filestorageService;
    }

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
                                    name = "Multipart Upload Success",
                                    value = SwaggerExamples.EXAMPLE_SUCCESS_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Errores de validación",
                    content = @Content(schema = @Schema(
                            example = SwaggerExamples.EXAMPLE_VALIDATION_ERROR))),
            @ApiResponse(responseCode = "401", description = "Error de autenticación",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_UNAUTHORIZED))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_INTERNAL_ERROR)))
    })
    @PostMapping("files-upload/generate-multipart-urls")
    public ResponseEntity<GenericResponse<MultipartUploadResponseDto>> generatePresignedUrls(@Valid @RequestBody MultipartUploadRequestDto request,
                                                                                             BindingResult bindingResult) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Iniciando generacion de url multiparts: usuario: {} nombre: {} contentType: {} bytes: {}"
                , username, request.getFilename(), request.getContentType(), request.getFileSizeBytes());
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: createMultipartUpload");
        MultipartUploadResponseDto result = filestorageService.createMultipartUpload(request);
        return ResponseEntity.ok(GenericResponse.success(result,
                String.format("Multipart upload iniciado correctamente para el archivo %s", result.getKey())));
    }

    @Operation(summary = "Completa el proceso de carga multipart",
            description = "Indica a S3 que todas las partes han sido cargadas y finaliza el proceso de subida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carga completada exitosamente",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_COMPLETE_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "Errores de validación",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_COMPLETE_VALIDATION_ERROR))),
            @ApiResponse(responseCode = "401", description = "Error de autenticación",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_UNAUTHORIZED))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = SwaggerExamples.EXAMPLE_INTERNAL_ERROR)))
    })
    @PostMapping("files-upload/complete-multiparts-upload")
    public ResponseEntity<GenericResponse<Void>> completeUpload(@Valid @RequestBody CompleteUploadRequestDto request,
                                                                BindingResult bindingResult) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Iniciando proceso para completar carga del archivo: usuario: {} key: {} uploadId: {}"
                , username, request.getKey(), request.getUploadId());
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: completeMultipartUpload");
        filestorageService.completeMultipartUpload(request);
        log.info("Finalizo proceso para completar carga del archivo: usuario: {} key: {} uploadId: {}"
                , username, request.getKey(), request.getUploadId());
        return ResponseEntity.ok(GenericResponse.success(null, "Subida completada exitosamente"));
    }

    /**
     * Procesa errores de validación en la entrada y construye una respuesta estándar.
     * @param bindingResult
     * @return Errores de validación en el request de entrada
     * @param <T>
     */
    private <T> ResponseEntity<GenericResponse<T>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        log.info(ERROR_MSG_VAL);
        return ResponseEntity.badRequest().body(GenericResponse.error(errors, ERROR_MSG_VAL));
    }

}
