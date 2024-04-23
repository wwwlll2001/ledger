package com.assignment.ledger.transaction.application.ports.in;

import com.assignment.ledger.transaction.application.dto.TransactionListDto;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import reactor.core.publisher.Flux;

public interface TransactionQueryService {

    Flux<TransactionListDto> retrieveTransactions(Long fromWalletId, Long toWalletId, TransactionStatus status);

}
