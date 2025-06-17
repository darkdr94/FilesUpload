package com.drv.filestorage.controller;

import com.drv.filestorage.common.ApiResponse;
import com.drv.filestorage.common.dto.CompleteUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadResponseDto;
import com.drv.filestorage.service.FilestorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        MultipartUploadResponseDto result = filestorageService.createMultipartUpload(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Multipart upload iniciado correctamente"));

    }

    @PostMapping("files-upload/complete-multiparts-upload")
    public ResponseEntity<ApiResponse<Void>>  completeUpload(@Valid @RequestBody CompleteUploadRequestDto request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(errors, ERROR_MSG_VAL)
            );
        }
        filestorageService.completeMultipartUpload(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Subida completada exitosamente"));
    }

}
