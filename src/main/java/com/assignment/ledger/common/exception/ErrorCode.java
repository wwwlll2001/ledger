package com.assignment.ledger.common.exception;

public enum ErrorCode {
    UNKNOWN("unknown_error"),
    RESOURCE_NOT_FOUND("resource_not_found"),
    INVALID_ACCOUNT_STATUS("invalid_account_status"),
    PROCESSING_TRANSACTION_EXIST("processing_transaction_exist"),
    INVALID_AMOUNT("invalid_amount"),
    INSUFFICIENT_FUNDS("insufficient_funds"),
    CONCURRENT_OPERATION("concurrent_operation"),
    WALLET_TYPE_NOT_CONSISTENT("wallet_type_not_consistent"),

    INVALID_TRANSACTION_STATUS("invalid_transaction_status");

    private String value;

    ErrorCode(String errCode) {
        this.value = errCode;
    }

    public String getValue() {
        return value;
    }
}