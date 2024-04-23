package com.assignment.ledger.common.exception;

public class ProcessingTransactionExistException extends RuntimeException {
    public ProcessingTransactionExistException(String message) {
        super(message);
    }
}
