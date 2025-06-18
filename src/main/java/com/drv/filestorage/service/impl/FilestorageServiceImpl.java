package com.drv.filestorage.service.impl;

import com.drv.filestorage.common.dto.*;
import com.drv.filestorage.common.entity.UploadedFileEntity;
import com.drv.filestorage.config.ParameterStoreService;
import com.drv.filestorage.exception.NoUploadedPartsException;
import com.drv.filestorage.service.FilestorageService;
import com.drv.filestorage.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    /**
     * Metodo que orquesta la generación de las url prefirmadas para subir un archivo a S3
     * @param request Datos del archivo: nombre, tamaño en bytes y tipo de contenido
     * @return listado de url prefirmadas, nombre y id del archivo
     */
    @Override
    public MultipartUploadResponseDto createMultipartUpload(MultipartUploadRequestDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String bucketName = resolveBucketName();

        String key = generateS3Key(username, request.getFilename());
        int partCount = calculatePartCount(request.getFileSizeBytes());
        String uploadId = initiateMultipartUpload(bucketName, key);

        // Crea el registro de la carga del archivo en la base de datos
        saveMetadataToDatabase(username, request, bucketName, key, uploadId);
        List<PartInfoResponseDto> parts = generatePresignedUrls(bucketName, key, uploadId, partCount);

        return new MultipartUploadResponseDto(key, uploadId, parts);
    }

    /**
     * Metodo que finaliza la carga del archivo en S3 y queda almacenado en el bucket
     * @param request informacion del archivo con los eTags de cada parte cargada
     */
    @Override
    public void completeMultipartUpload(CompleteUploadRequestDto request) {
        String bucketName = resolveBucketName();

        validateUploadedPartsExist(bucketName, request);

        List<CompletedPart> completedParts = buildCompletedParts(request);

        CompleteMultipartUploadRequest completeRequest = buildCompleteMultipartUploadRequest(
                bucketName, request, buildCompletedMultipartUpload(completedParts));

        s3Client.completeMultipartUpload(completeRequest);

        //Actualiza el estado a completado del registro en la BD
        uploadedFileService.updateStatus(request.getUploadId(), "completed");
    }

    /**
     * Obtiene el nombre del bucket de las variables de entorno
     * @return el nombre del bucket
     */
    private String resolveBucketName() {
        return parameterStoreService.getParameter(bucketNameParam);
    }

    /**
     * Genera la key que se usará en S3 para el archivo multipart.
     * @param username nombre del usuario
     * @param filename nombre del archivo que le dio el usuario
     * @return nombre unico y completo del archivo
     */
    private String generateS3Key(String username, String filename) {
        return String.format("%s/%d/%02d/%s_%s",
                username,
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue(),
                UUID.randomUUID(),
                filename
        );
    }

    /**
     * Calcula el numero de partes que necesita el archivo en función de su tamaño.
     * @param fileSizeBytes Tamaño del archivo
     * @return cantidad de partes
     */
    private int calculatePartCount(long fileSizeBytes) {
        long partSizeBytes = partSizeMegaBytes * 1024 * 1024;
        int partCount = (int) Math.ceil((double) fileSizeBytes / partSizeBytes);
        if (partCount > 10_000) {
            log.error("El tamaño de partes excedio el limite");
            throw new IllegalArgumentException("El archivo requiere más de 10,000 partes. Reduce el tamaño de parte o archivo.");
        }
        return partCount;
    }

    /**
     * Inicia el proceso de carga multipart en S3 y devuelve el uploadId.
     * @param bucketName nombre del bucket
     * @param key nombre del archivo
     * @return uploadID identificador del proceso de la carga del archivo
     */
    private String initiateMultipartUpload(String bucketName, String key) {
        return s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()).uploadId();
    }

    /**
     * Guarda los metadatos del archivo en la base de datos.
     * @param username usuario que sube el archivo
     * @param request datos del archivo a subir
     * @param bucketName nombre del bucket
     * @param key clave del archivo en S3
     * @param uploadId ID del upload multipart
     */
    private void saveMetadataToDatabase(String username, MultipartUploadRequestDto request,
                                        String bucketName, String key, String uploadId) {
        UploadedFileEntity file = new UploadedFileEntity();
        file.setFilename(request.getFilename());
        file.setContentType(request.getContentType());
        file.setS3Key(key);
        file.setUploadId(uploadId);
        file.setBucketName(bucketName);
        file.setSizeBytes(request.getFileSizeBytes());
        file.setUploadedBy(username);
        uploadedFileService.saveFile(file);
        log.info("Registro del archivo almacenado en BD");
    }

    /**
     * Genera las URLs prefirmadas para cada parte del archivo que se va a subir a S3.
     * @param bucketName nombre del bucket
     * @param key nombre del archivo
     * @param uploadId identificador de la carga del archivo
     * @param partCount numero de partes
     * @return Listado con las url prefirmadas para subir el archivo
     */
    private List<PartInfoResponseDto> generatePresignedUrls(String bucketName, String key, String uploadId, int partCount) {
        return IntStream.rangeClosed(1, partCount)
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
    }

    /**
     * Valida que existan partes subidas en S3 para el `uploadId` ingresado.
     * @param bucketName nombre del bucket
     * @param request id del archivo y su listado de partes
     */
    private void validateUploadedPartsExist(String bucketName, CompleteUploadRequestDto request) {
        try {
            s3Client.listParts(ListPartsRequest.builder()
                    .bucket(bucketName)
                    .key(request.getKey())
                    .uploadId(request.getUploadId())
                    .build());
        } catch (Exception e) {
            log.error("No se encontraron partes subidas para el archivo {} con uploadId {}", request.getKey(), request.getUploadId());
            throw new NoUploadedPartsException(request.getKey(), request.getUploadId());
        }
    }

    /**
     * Construye la lista de partes completadas ordenadas por su número de parte.
     * @param request id del archivo y su listado de partes
     * @return Lista de partes completadas
     */
    private List<CompletedPart> buildCompletedParts(CompleteUploadRequestDto request) {
        return request.getParts().stream()
                .sorted(Comparator.comparingInt(CompletedPartRequestDto::getPartNumber))
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getETag())
                        .build())
                .toList();
    }

    /**
     * Crea un objeto `CompletedMultipartUpload` con la lista de partes completadas.
     * @param completedParts lista de partes completadas
     * @return Objeto CompletedMultipartUpload
     */
    private CompletedMultipartUpload buildCompletedMultipartUpload(List<CompletedPart> completedParts) {
        return CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();
    }

    /**
     *Crea el objeto "CompleteMultipartUploadRequest" necesario para completar la carga del archivo
     * @param bucketName nombre del bucket
     * @param request id del archivo y su listado de partes
     * @param completedMultipartUpload
     * @return Objeto de tipo "CompleteMultipartUploadRequest"
     */
    private CompleteMultipartUploadRequest buildCompleteMultipartUploadRequest(
            String bucketName,
            CompleteUploadRequestDto request,
            CompletedMultipartUpload completedMultipartUpload
    ) {
        return CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(request.getKey())
                .uploadId(request.getUploadId())
                .multipartUpload(completedMultipartUpload)
                .build();
    }
}
