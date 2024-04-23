package com.assignment.ledger.common.exception;

public class ErrorResult {

    private ErrorCode errorCode;
    private String message;

    public ErrorResult() {

    }

    public ErrorResult(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResult(ErrorCode errorCode, String message) {
        this(errorCode);
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
