package com.projectmanagement.seller.exception;

public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public CustomException(String message) {
        super(message);
        this.errorCode = "BAD_REQUEST";
        this.details = message;
    }

    public CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = message;
    }

    public CustomException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}
