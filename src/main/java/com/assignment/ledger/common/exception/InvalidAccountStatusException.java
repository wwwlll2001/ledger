package com.assignment.ledger.common.exception;

public class InvalidAccountStatusException extends RuntimeException {
    public InvalidAccountStatusException(String message) {
        super(message);
    }
}
