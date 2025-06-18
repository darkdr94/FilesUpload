package com.drv.filestorage.controller;

import com.drv.filestorage.common.ApiResponse;
import com.drv.filestorage.common.dto.CompleteUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadResponseDto;
import com.drv.filestorage.service.FilestorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FilesUploadController {

    @Autowired
    private FilestorageService filestorageService;

    private static final String ERROR_MSG_VAL = "Errores de validación";

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Info del servicio");
    }

    @PostMapping("files-upload/generate-multipart-urls")
    public ResponseEntity<ApiResponse<MultipartUploadResponseDto>> generatePresignedUrls(@Valid @RequestBody MultipartUploadRequestDto request,
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
                    ApiResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: createMultipartUpload");
        MultipartUploadResponseDto result = filestorageService.createMultipartUpload(request);
        return ResponseEntity.ok(ApiResponse.success(result,
                String.format("Multipart upload iniciado correctamente para el archivo {}", result.getKey())));
    }

    @PostMapping("files-upload/complete-multiparts-upload")
    public ResponseEntity<ApiResponse<Void>> completeUpload(@Valid @RequestBody CompleteUploadRequestDto request,
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
                    ApiResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        log.info("Datos validados correctamente, iniciando llamado al servicio: completeMultipartUpload");
        filestorageService.completeMultipartUpload(request);
        log.info("Finalizo proceso para completar carga del archivo: usuario: {} key: {} uploadId: {}"
                , username, request.getKey(), request.getUploadId());
        return ResponseEntity.ok(ApiResponse.success(null, "Subida completada exitosamente"));
    }

}
