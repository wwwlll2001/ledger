package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.dto.document.TransactionDocument;
import com.assignment.ledger.transaction.application.dto.document.TransactionHistoryDocument;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionSynchronizeEsService implements TransactionSynchronizeService {

    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    private final WalletRepository walletRepository;

    public TransactionSynchronizeEsService(ReactiveElasticsearchOperations reactiveElasticsearchOperations,
                                           WalletRepository walletRepository) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
        this.walletRepository = walletRepository;
    }

    @Override
    public void synchronizeTransaction(TransactionDto transactionDto) {
        Wallet fromWallet = walletRepository.findById(transactionDto.getFromWalletId()).orElse(null);
        Wallet toWallet = walletRepository.findById(transactionDto.getToWalletId()).orElse(null);

        TransactionDocument transactionDocument = TransactionDocument.from(transactionDto,
                                                                           fromWallet.getName(),
                                                                           toWallet.getName());

        reactiveElasticsearchOperations.save(transactionDocument)
                                       .doOnNext(saved -> log.info("save transaction: " + transactionDocument))
                                       .doOnError(error -> log.error("save error", error))
                                       .subscribe();

        TransactionHistoryDocument transactionHistoryDocument = TransactionHistoryDocument.from(transactionDto,
                                                                                                fromWallet.getName(),
                                                                                                toWallet.getName());
        reactiveElasticsearchOperations.save(transactionHistoryDocument)
                .doOnNext(saved -> log.info("save history transaction document: " + transactionHistoryDocument))
                .doOnError(error -> log.error("save error", error))
                .subscribe();
    }
}
