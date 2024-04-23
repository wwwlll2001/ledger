package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.transaction.application.dto.TransactionDto;


public interface TransactionSynchronizeService {

    /**
     * Synchronize transaction data to elasticsearch.
     *
     * @param transactionDto transaction data
     */
    void synchronizeTransaction(TransactionDto transactionDto);
}
