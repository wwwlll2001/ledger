package com.assignment.ledger.transaction.adapter.web;

import com.assignment.ledger.transaction.application.dto.TransactionListDto;
import com.assignment.ledger.transaction.application.ports.in.TransactionQueryService;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Dedicated controller for query to implement CQRS
 * Queries will be handled from Elasticsearch to release the write database pressure and get better performance and
 * scalability.
 *
 */
@RestController
@RequestMapping("/transactions")
public class TransactionQueryController implements TransactionQueryApi {

    private final TransactionQueryService transactionQueryService;

    public TransactionQueryController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    /**
     * Retrieve transactions conditionally.
     * This is a simple query, condition could be enriched afterward.
     *
     * @param fromWalletId from wallet id
     * @param toWalletId to wallet id
     * @param status transaction status
     * @return
     */
    @GetMapping
    public Flux<TransactionListDto> retrieveTransactions(
                                                            @RequestParam(required = false) Long fromWalletId,
                                                            @RequestParam(required = false) Long toWalletId,
                                                            @RequestParam(required = false) TransactionStatus status) {
        return transactionQueryService.retrieveTransactions(fromWalletId, toWalletId, status);
    }
}
