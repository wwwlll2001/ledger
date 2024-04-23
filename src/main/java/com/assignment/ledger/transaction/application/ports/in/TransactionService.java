package com.assignment.ledger.transaction.application.ports.in;

import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.dto.StartTransactionResponse;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.dto.UpdateTransactionRequest;

import java.util.List;

public interface TransactionService {

    /**
     * Save the transaction request and broadcast this event.
     *
     * @param startTransactionRequests start transaction requests.
     * @return the transaction data with id so that client could get the transaction result via the transaction id.
     */
    StartTransactionResponse startTransaction(List<StartTransactionRequest> startTransactionRequests);

    /**
     * Process the transaction, and send the transaction result as well as broadcast the wallet balance change.
     *
     * @param transactionDto transaction data
     */
    void processTransaction(TransactionDto transactionDto);

    /**
     * Process the transaction, and send the transaction result as well as broadcast the wallet balance change.
     *
     * @param transactionDto update transaction data
     */
    void updateTransaction(TransactionDto transactionDto);

    /**
     * Send the update request event.
     *
     * @param id transaction id
     * @param updateTransactionRequest update transaction data
     */
    void startUpdateTransaction(Long id, UpdateTransactionRequest updateTransactionRequest);
}
