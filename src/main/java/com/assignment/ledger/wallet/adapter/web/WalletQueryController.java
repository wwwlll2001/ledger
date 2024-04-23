package com.assignment.ledger.wallet.adapter.web;

import com.assignment.ledger.wallet.application.ports.in.WalletQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/wallets")
public class WalletQueryController implements WalletQueryService {

    private final WalletQueryService walletQueryService;

    @Autowired
    public WalletQueryController(WalletQueryService walletQueryService) {
        this.walletQueryService = walletQueryService;
    }

    /**
     * Get the balance at specific timestamp.
     *
     * @param walletId wallet id
     * @param queryTime the specific timestamp for the queried wallet balance
     * @return wallet balance at specific timestamp
     */
    @GetMapping("/{id}/balance")
    public Mono<BigDecimal> getWalletBalanceAt(@PathVariable("id") Long walletId,
                                               @RequestParam LocalDateTime queryTime) {
        return walletQueryService.getWalletBalanceAt(walletId, queryTime);
    }
}
