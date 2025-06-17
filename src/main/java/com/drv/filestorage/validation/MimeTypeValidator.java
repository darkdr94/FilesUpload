package com.drv.filestorage.validation;

import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class MimeTypeValidator implements ConstraintValidator<ValidMimeType, MultipartUploadRequestDto> {

    private static final Map<String, String> EXTENSION_TO_MIME = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("mp4", "video/mp4"),
            Map.entry("zip", "application/zip"),
            Map.entry("txt", "text/plain"),
            Map.entry("csv", "text/csv"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            // puedes agregar más aquí
    );

    @Override
    public boolean isValid(MultipartUploadRequestDto request, ConstraintValidatorContext context) {
        if (request == null || request.getFilename() == null || request.getContentType() == null) {
            return true;
        }

        String extension = getFileExtension(request.getFilename());
        String expectedMime = EXTENSION_TO_MIME.get(extension);

        if (expectedMime == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Extensión de archivo no permitida: " + extension)
                    .addPropertyNode("filename")
                    .addConstraintViolation();
            return false;
        }

        if (!expectedMime.equalsIgnoreCase(request.getContentType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El tipo MIME no coincide con la extensión: se esperaba " + expectedMime)
                    .addPropertyNode("contentType")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot != -1 && lastDot < filename.length() - 1)
                ? filename.substring(lastDot + 1).toLowerCase()
                : "";
    }
}
