package com.drv.filestorage.controller;

import com.drv.filestorage.common.GenericResponse;
import com.drv.filestorage.common.dto.CompleteUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadResponseDto;
import com.drv.filestorage.common.dto.PartInfoResponseDto;
import com.drv.filestorage.service.FilestorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilesUploadControllerTest {

    private FilestorageService filestorageService;
    private FilesUploadController controller;

    @BeforeEach
    void setUp() {
        filestorageService = mock(FilestorageService.class);
        controller = new FilesUploadController(filestorageService);
    }

    @BeforeEach
    void setUpSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void testGeneratePresignedUrls_validRequest_returnsSuccessResponse() {
        MultipartUploadRequestDto request = new MultipartUploadRequestDto();
        request.setFilename("test.txt");
        request.setFileSizeBytes(1024L);
        request.setContentType("text/plain");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        MultipartUploadResponseDto mockResponse = new MultipartUploadResponseDto(
                "s3/key", UUID.randomUUID().toString(), List.of(new PartInfoResponseDto(1, "https://fake-url"))
        );
        when(filestorageService.createMultipartUpload(request)).thenReturn(mockResponse);

        ResponseEntity<GenericResponse<MultipartUploadResponseDto>> response = controller.generatePresignedUrls(request, bindingResult);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("s3/key", response.getBody().getData().getKey());
    }

    @Test
    void testGeneratePresignedUrls_withValidationErrors_returnsBadRequest() {
        MultipartUploadRequestDto request = new MultipartUploadRequestDto();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "filename", "Filename is required")
        ));

        ResponseEntity<?> response = controller.generatePresignedUrls(request, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        GenericResponse<?> body = (GenericResponse<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Errores de validación", body.getMessage());
        assertEquals("Filename is required", ((Map<?, ?>) body.getErrors()).get("filename"));
    }

    @Test
    void testCompleteUpload_validRequest_returnsSuccess() {
        CompleteUploadRequestDto request = new CompleteUploadRequestDto();
        request.setKey("s3/key");
        request.setUploadId("upload-123");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<GenericResponse<Void>> response = controller.completeUpload(request, bindingResult);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Subida completada exitosamente", response.getBody().getMessage());

        verify(filestorageService).completeMultipartUpload(request);
    }

    @Test
    void testCompleteUpload_withValidationErrors_returnsBadRequest() {
        CompleteUploadRequestDto request = new CompleteUploadRequestDto();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "uploadId", "Upload ID is required")
        ));

        ResponseEntity<?> response = controller.completeUpload(request, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        GenericResponse<?> body = (GenericResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Upload ID is required", ((Map<?, ?>) body.getErrors()).get("uploadId"));
    }

    @Test
    void testHealthCheck_returnsOk() {
        ResponseEntity<String> response = controller.healthCheck();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("OK", response.getBody());
    }

    @Test
    void testInfo_returnsInfoMessage() {
        ResponseEntity<String> response = controller.info();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Info del servicio", response.getBody());
    }
}
