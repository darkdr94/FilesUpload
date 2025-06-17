package com.drv.filestorage.service;

import com.drv.filestorage.common.entity.UploadedFileEntity;
import com.drv.filestorage.repository.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UploadedFileService {

    private final UploadedFileRepository uploadedFileRepository;

    public UploadedFileService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
    }

    @Transactional
    public UploadedFileEntity saveFile(UploadedFileEntity file) {
        return uploadedFileRepository.save(file);
    }

    @Transactional
    public Optional<UploadedFileEntity> updateStatus(UUID id, String newStatus) {
        Optional<UploadedFileEntity> fileOpt = uploadedFileRepository.findById(id);
        fileOpt.ifPresent(file -> {
            file.setStatus(newStatus);
            uploadedFileRepository.save(file);
        });
        return fileOpt;
    }

    public void updateStatus(String uploadId, String status) {
        UploadedFileEntity file = uploadedFileRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));
        file.setStatus(status);
        uploadedFileRepository.save(file);
    }
}
