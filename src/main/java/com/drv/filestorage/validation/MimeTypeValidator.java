package com.drv.filestorage.validation;

import com.drv.filestorage.common.dto.MultipartUploadRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

/**
 * Validador lógico de la anotación personalizada @ValidMimeType
 */
public class MimeTypeValidator implements ConstraintValidator<ValidMimeType, MultipartUploadRequestDto> {

    private static final Map<String, String> EXTENSION_TO_MIME = Map.ofEntries(
            // Imágenes
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("bmp", "image/bmp"),
            Map.entry("webp", "image/webp"),
            Map.entry("svg", "image/svg+xml"),

            // Documentos
            Map.entry("pdf", "application/pdf"),
            Map.entry("txt", "text/plain"),
            Map.entry("csv", "text/csv"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),

            // Audio
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("wav", "audio/wav"),
            Map.entry("flac", "audio/flac"),
            Map.entry("ogg", "audio/ogg"),
            Map.entry("m4a", "audio/mp4"),

            // Video
            Map.entry("mp4", "video/mp4"),
            Map.entry("mkv", "video/x-matroska"),
            Map.entry("mov", "video/quicktime"),
            Map.entry("avi", "video/x-msvideo"),
            Map.entry("flv", "video/x-flv"),
            Map.entry("webm", "video/webm"),

            // Archivos comprimidos
            Map.entry("zip", "application/zip"),
            Map.entry("rar", "application/vnd.rar"),
            Map.entry("7z", "application/x-7z-compressed"),
            Map.entry("tar", "application/x-tar"),
            Map.entry("gz", "application/gzip"),

            // Imágenes de disco
            Map.entry("iso", "application/x-iso9660-image"),
            Map.entry("vmdk", "application/x-vmdk"),
            Map.entry("vhd", "application/x-vhd"),

            // Bases de datos / backups
            Map.entry("bak", "application/octet-stream"),
            Map.entry("sql", "application/sql"),
            Map.entry("db", "application/x-sqlite3"),

            // Archivos científicos / grandes datos
            Map.entry("hdf5", "application/x-hdf5"),
            Map.entry("nc", "application/x-netcdf"),
            Map.entry("mat", "application/x-matlab-data"),

            // Diseño / modelado 3D
            Map.entry("psd", "image/vnd.adobe.photoshop"),
            Map.entry("ai", "application/postscript"),
            Map.entry("indd", "application/x-indesign"),
            Map.entry("blend", "application/x-blender"),
            Map.entry("fbx", "application/octet-stream"),
            Map.entry("obj", "application/octet-stream")
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
