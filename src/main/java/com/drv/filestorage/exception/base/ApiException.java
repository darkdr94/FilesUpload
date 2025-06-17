package com.drv.filestorage.exception.base;

public abstract class ApiException extends RuntimeException {
    private final int status;
    private final String code;

    protected ApiException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}