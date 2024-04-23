package com.assignment.ledger.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler
 */
@RestControllerAdvice
@Slf4j
public class LedgerExceptionHandler {

    @ExceptionHandler(WalletTypeNotConsistentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleWalletTypeNotConsistentException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.WALLET_TYPE_NOT_CONSISTENT, e.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleInvalidAmountException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.INVALID_AMOUNT, e.getMessage());
    }

    @ExceptionHandler(InSufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleInSufficientFundsException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.INSUFFICIENT_FUNDS, e.getMessage());
    }

    @ExceptionHandler(ProcessingTransactionExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleProcessingTransactionExistException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.PROCESSING_TRANSACTION_EXIST, e.getMessage());
    }

    @ExceptionHandler(InvalidAccountStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResult handleAccountStatusException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.INVALID_ACCOUNT_STATUS, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResult handleResourceNotFoundException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResult(ErrorCode.RESOURCE_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResult handleOtherException(Throwable e) {
        log.error("system error", e);
        return new ErrorResult(ErrorCode.UNKNOWN, e.getMessage());
    }
}
