package com.drv.filestorage.service.impl;

import com.drv.filestorage.TestUtils;
import com.drv.filestorage.common.dto.*;
import com.drv.filestorage.config.ParameterStoreService;
import com.drv.filestorage.service.UploadedFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilestorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private ParameterStoreService parameterStoreService;

    @Mock
    private UploadedFileService uploadedFileService;

    @InjectMocks
    private FilestorageServiceImpl filestorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setear los valores inyectados por @Value manualmente
        filestorageService = new FilestorageServiceImpl(
                s3Client,
                s3Presigner,
                parameterStoreService,
                uploadedFileService
        );
        // Usar reflexión para inyectar los valores de las propiedades @Value
        TestUtils.setField(filestorageService, "bucketNameParam", "test-bucket-param");
        TestUtils.setField(filestorageService, "presignDurationMinutes", 15);
        TestUtils.setField(filestorageService, "partSizeMegaBytes", 5);
    }

    @Test
    void testCreateMultipartUpload_returnsValidResponse() {
        // Arrange
        String bucketName = "test-bucket";
        String uploadId = "upload-123";

        when(parameterStoreService.getParameter("test-bucket-param")).thenReturn(bucketName);

        MultipartUploadRequestDto requestDto = new MultipartUploadRequestDto();
        requestDto.setFilename("archivo.txt");
        requestDto.setFileSizeBytes(10 * 1024 * 1024L); // 10MB
        requestDto.setContentType("text/plain");

        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(CreateMultipartUploadResponse.builder()
                        .uploadId(uploadId)
                        .build());

        when(s3Presigner.presignUploadPart(any(UploadPartPresignRequest.class)))
                .thenAnswer(invocation -> {
                    UploadPartPresignRequest presignReq = invocation.getArgument(0);
                    int partNumber = presignReq.uploadPartRequest().partNumber();

                    PresignedUploadPartRequest mockPresignedRequest = mock(PresignedUploadPartRequest.class);
                    when(mockPresignedRequest.url()).thenReturn(
                            URI.create("https://s3.amazonaws.com/fake-url/part" + partNumber).toURL()
                    );

                    return mockPresignedRequest;
                });


        // Simular autenticación
        TestUtils.mockAuthentication("test-user");

        // Act
        MultipartUploadResponseDto response = filestorageService.createMultipartUpload(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(uploadId, response.getUploadId());
        assertEquals(2, response.getUrls().size());

        PartInfoResponseDto part1 = response.getUrls().get(0);
        assertEquals(1, part1.getPartNumber());
        assertTrue(part1.getPresignedUrl().contains("part1"));
    }

    @Test
    void testCompleteMultipartUpload_executesSuccessfully() {
        // Arrange
        when(parameterStoreService.getParameter("test-bucket-param")).thenReturn("test-bucket");

        // Simula que hay partes en S3
        ListPartsResponse listPartsResponse = ListPartsResponse.builder().build();
        when(s3Client.listParts(any(ListPartsRequest.class))).thenReturn(listPartsResponse);

        // DTO simulado
        CompleteUploadRequestDto request = new CompleteUploadRequestDto();
        request.setKey("test-key");
        request.setUploadId("upload-123");

        CompletedPartRequestDto part1 = new CompletedPartRequestDto();
        part1.setPartNumber(1);
        part1.setETag("etag-1");

        CompletedPartRequestDto part2 = new CompletedPartRequestDto();
        part2.setPartNumber(2);
        part2.setETag("etag-2");

        request.setParts(List.of(part1, part2));

        // Act
        filestorageService.completeMultipartUpload(request);

        // Assert
        verify(s3Client, times(1)).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
        verify(uploadedFileService).updateStatus("upload-123", "completed");
    }
}
