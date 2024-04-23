package com.assignment.ledger.transaction.adapter.web;

import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.dto.StartTransactionResponse;
import com.assignment.ledger.transaction.application.dto.UpdateTransactionRequest;
import com.assignment.ledger.transaction.application.ports.in.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController implements TransactionApi {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Start transactions, support batch transaction.
     * After transaction started, it is in "processing" status, the real handling will be conducted asynchronously.
     * Client should subscribe relevant message to get update result.
     *
     * @param startTransactionRequests start transaction requests, support batch transactions
     * @return started transaction ids
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StartTransactionResponse startTransaction(
                                           @Valid @RequestBody List<StartTransactionRequest> startTransactionRequests) {
        return transactionService.startTransaction(startTransactionRequests);
    }


    /**
     * Request to update transaction, the update process will be conducted asynchronously.
     * Client should subscribe relevant message to get update result.
     *
     * @param id id of transaction to be updated
     * @param updateTransactionRequest new transaction data
     */
    @PutMapping("/{id}")
    public void updateTransaction(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest) {
        transactionService.startUpdateTransaction(id, updateTransactionRequest);
    }
}
