package com.assignment.ledger.common.exception;

public class InSufficientFundsException extends RuntimeException {
    public InSufficientFundsException(String message) {
        super(message);
    }
}
