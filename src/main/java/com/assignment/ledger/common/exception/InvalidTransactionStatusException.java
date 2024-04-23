package com.assignment.ledger.common.exception;

public class InvalidTransactionStatusException extends RuntimeException {
    public InvalidTransactionStatusException(String message) {
        super(message);
    }
}
