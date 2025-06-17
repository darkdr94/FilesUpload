package com.drv.filestorage.exception;

import com.drv.filestorage.exception.base.ApiException;
import org.springframework.http.HttpStatus;

public class NoUploadedPartsException extends ApiException {
    public NoUploadedPartsException(String key, String uploadId) {
        super(
                "NO_UPLOADED_PARTS",
                String.format("No se encontraron partes subidas para el archivo [%s] con uploadId [%s]", key, uploadId),
                HttpStatus.BAD_REQUEST.value()
        );
    }
}