package com.drv.filestorage.service;

import com.drv.filestorage.common.entity.UploadedFileEntity;
import com.drv.filestorage.repository.UploadedFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadedFileServiceTest {

    private UploadedFileRepository uploadedFileRepository;
    private UploadedFileService uploadedFileService;

    @BeforeEach
    void setUp() {
        uploadedFileRepository = mock(UploadedFileRepository.class);
        uploadedFileService = new UploadedFileService(uploadedFileRepository);
    }

    @Test
    void testSaveFile_successfullySaves() {
        UploadedFileEntity file = new UploadedFileEntity();
        file.setFilename("test.txt");

        when(uploadedFileRepository.save(file)).thenReturn(file);

        UploadedFileEntity result = uploadedFileService.saveFile(file);

        assertEquals("test.txt", result.getFilename());
        verify(uploadedFileRepository).save(file);
    }

    @Test
    void testUpdateStatusByUUID_foundAndUpdated() {
        UUID id = UUID.randomUUID();
        UploadedFileEntity file = new UploadedFileEntity();
        file.setStatus("pending");

        when(uploadedFileRepository.findById(id)).thenReturn(Optional.of(file));
        when(uploadedFileRepository.save(file)).thenReturn(file);

        Optional<UploadedFileEntity> result = uploadedFileService.updateStatus(id, "completed");

        assertTrue(result.isPresent());
        assertEquals("completed", result.get().getStatus());
        verify(uploadedFileRepository).save(file);
    }

    @Test
    void testUpdateStatusByUUID_notFound() {
        UUID id = UUID.randomUUID();

        when(uploadedFileRepository.findById(id)).thenReturn(Optional.empty());

        Optional<UploadedFileEntity> result = uploadedFileService.updateStatus(id, "completed");

        assertFalse(result.isPresent());
        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusByUploadId_success() {
        String uploadId = "upload-123";
        UploadedFileEntity file = new UploadedFileEntity();
        file.setStatus("pending");

        when(uploadedFileRepository.findByUploadId(uploadId)).thenReturn(Optional.of(file));
        when(uploadedFileRepository.save(file)).thenReturn(file);

        uploadedFileService.updateStatus(uploadId, "completed");

        assertEquals("completed", file.getStatus());
        verify(uploadedFileRepository).save(file);
    }

    @Test
    void testUpdateStatusByUploadId_notFound_shouldThrow() {
        String uploadId = "missing-upload";

        when(uploadedFileRepository.findByUploadId(uploadId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uploadedFileService.updateStatus(uploadId, "completed");
        });

        assertEquals("Archivo no encontrado", exception.getMessage());
        verify(uploadedFileRepository, never()).save(any());
    }
}
