package com.assignment.ledger.wallet.application.service;

import com.assignment.ledger.wallet.application.dto.BalanceChangeDto;
import com.assignment.ledger.wallet.application.dto.WalletBalanceHistoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletSynchronizeEsService implements WalletSynchronizeService {

    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public WalletSynchronizeEsService(ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }

    @Override
    public void synchronizeBalanceChange(BalanceChangeDto balanceChangeDto) {
        //if under high concurrency env, the synchronized data should not be read from DB, should use event data
        // directly
        WalletBalanceHistoryDocument walletBalanceHistoryDocument = WalletBalanceHistoryDocument.from(balanceChangeDto);
        reactiveElasticsearchOperations.save(walletBalanceHistoryDocument)
                .doOnNext(saved -> log.info("save wallet balance change: " + walletBalanceHistoryDocument))
                .doOnError(error -> log.error("save error", error))
                .subscribe();
    }
}
