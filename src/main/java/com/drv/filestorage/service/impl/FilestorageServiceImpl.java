package com.drv.filestorage.service.impl;

import com.drv.filestorage.common.dto.*;
import com.drv.filestorage.common.entity.UploadedFileEntity;
import com.drv.filestorage.config.ParameterStoreService;
import com.drv.filestorage.exception.NoUploadedPartsException;
import com.drv.filestorage.service.FilestorageService;
import com.drv.filestorage.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FilestorageServiceImpl implements FilestorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ParameterStoreService parameterStoreService;
    private final UploadedFileService uploadedFileService;

    @Value("${app.ssm.bucket-name-param}")
    private String bucketNameParam;

    @Value("${app.s3.presign-duration-minutes}")
    private int presignDurationMinutes;

    @Value("${app.s3.part-size-megabytes}")
    private long partSizeMegaBytes;

    @Override
    public MultipartUploadResponseDto createMultipartUpload(MultipartUploadRequestDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String bucketName = parameterStoreService.getParameter(bucketNameParam);

        String key = String.format("%s/%d/%02d/%s_%s",
                username,
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue(),
                UUID.randomUUID(),
                request.getFilename()
        );

        long fileSize = request.getFileSizeBytes();
        long partSizeBytes = partSizeMegaBytes * 1024 * 1024;
        int partCount = (int) Math.ceil((double) fileSize / partSizeBytes);
        if (partCount > 10_000) {
            throw new IllegalArgumentException("El archivo requiere más de 10,000 partes. Reduce el tamaño de parte o archivo.");
        }

        String uploadId = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()).uploadId();

        // Guardar en la base de datos
        UploadedFileEntity file = new UploadedFileEntity();
        file.setFilename(request.getFilename());
        file.setContentType(request.getContentType());
        file.setS3Key(key);
        file.setUploadId(uploadId);
        file.setBucketName(bucketName);
        file.setSizeBytes(fileSize);
        file.setUploadedBy(username);

        uploadedFileService.saveFile(file);

        List<PartInfoResponseDto> parts = IntStream.rangeClosed(1, partCount)
                .mapToObj(partNumber -> {
                    UploadPartRequest partRequest = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .build();

                    UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                            .uploadPartRequest(partRequest)
                            .build();

                    String url = s3Presigner.presignUploadPart(presignRequest).url().toString();
                    return new PartInfoResponseDto(partNumber, url);
                })
                .toList();

        return new MultipartUploadResponseDto(key, uploadId, parts);
    }

    @Override
    public void completeMultipartUpload(CompleteUploadRequestDto request) {
        String bucketName = parameterStoreService.getParameter(bucketNameParam);

        try {
            s3Client.listParts(ListPartsRequest.builder()
                    .bucket(bucketName)
                    .key(request.getKey())
                    .uploadId(request.getUploadId())
                    .build());
        }
        catch (Exception e){
            throw new NoUploadedPartsException(request.getKey(), request.getUploadId());
        }

        List<CompletedPart> completedParts = request.getParts().stream()
                .sorted(Comparator.comparingInt(CompletedPartRequestDto::getPartNumber))
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getETag())
                        .build())
                .toList();

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(request.getKey())
                .uploadId(request.getUploadId())
                .multipartUpload(completedMultipartUpload)
                .build();

        s3Client.completeMultipartUpload(completeRequest);
        uploadedFileService.updateStatus(request.getUploadId(), "completed");
    }


}
