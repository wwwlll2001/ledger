package com.assignment.ledger.transaction.domain;

/**
 * TransactionStatus enum.
 *
 * <p>PROCESSING: when transaction is just started, the status is PROCESSING
 *
 * <p>CLEARED: if processing transaction successfully, the status should be set to CLEARED.
 *
 * <p>FAILED: if processing transaction failed, the status should be set to FAILED.
 */
public enum TransactionStatus {
    PROCESSING, CLEARED, FAILED
}
