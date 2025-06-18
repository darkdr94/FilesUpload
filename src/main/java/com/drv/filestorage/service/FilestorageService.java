package com.drv.filestorage.service;

import com.drv.filestorage.common.dto.CompleteUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import com.drv.filestorage.common.dto.MultipartUploadResponseDto;

/**
 * Define el contrto de los servicios para carga de archivos multipart de S3
 */
public interface FilestorageService {

    MultipartUploadResponseDto createMultipartUpload(MultipartUploadRequestDto request);
    void completeMultipartUpload(CompleteUploadRequestDto request);

}
