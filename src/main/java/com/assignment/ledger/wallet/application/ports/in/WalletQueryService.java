package com.assignment.ledger.wallet.application.ports.in;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface WalletQueryService {

    /**
     * Get the wallet balance at specific timestamp
     *
     * @param walletId wallet id
     * @param queryTime query timestamp
     * @return wallet balance at specific timestamp
     */
    Mono<BigDecimal> getWalletBalanceAt(Long walletId, LocalDateTime queryTime);
}
