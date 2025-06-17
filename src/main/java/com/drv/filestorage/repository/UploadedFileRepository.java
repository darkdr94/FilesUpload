package com.drv.filestorage.repository;

import com.drv.filestorage.common.entity.UploadedFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, UUID> {
    Optional<UploadedFileEntity> findByUploadId(String uploadId);
}
