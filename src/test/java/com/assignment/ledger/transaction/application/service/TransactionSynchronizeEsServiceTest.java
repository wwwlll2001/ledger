package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.ApiTestBase;
import com.assignment.ledger.account.application.ports.out.AccountRepository;
import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.entity.application.ports.out.LedgerEntityRepository;
import com.assignment.ledger.entity.domain.LedgerEntity;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.dto.document.TransactionDocument;
import com.assignment.ledger.transaction.application.ports.out.TransactionRepository;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.AssetType;
import com.assignment.ledger.wallet.domain.Wallet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class TransactionSynchronizeEsServiceTest extends ApiTestBase {

    @Autowired
    private ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Autowired
    private TransactionSynchronizeEsService transactionSynchronizeEsService;

    @Autowired
    private LedgerEntityRepository ledgerEntityRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void should_save_transaction_document_successfully() {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        prepareTransaction(50L, fromWallet, toWallet, TransactionStatus.PROCESSING);

        TransactionDto transactionDto = TransactionDto.builder().id(1L)
                .amount(BigDecimal.valueOf(50L))
                .toWalletId(2L)
                .fromWalletId(1L)
                .status(TransactionStatus.PROCESSING)
                .build();
        transactionSynchronizeEsService.synchronizeTransaction(transactionDto);

        Criteria criteria = new Criteria("id").is(1L);
        reactiveElasticsearchOperations.search(new CriteriaQuery(criteria), TransactionDocument.class)
                .map(SearchHit::getContent)
                .subscribe(transactionDocument -> {
                    assertThat(transactionDocument.getToWalletId(), is(2L));
                    assertThat(transactionDocument.getAmount(), is(BigDecimal.valueOf(50L)));
                });
    }

    @NotNull
    private Account prepareAccount(AccountStatus accountStatus) {
        LedgerEntity ledgerEntity = LedgerEntity.builder().name("entity1").build();
        ledgerEntityRepository.save(ledgerEntity);
        Account account = Account.builder()
                .status(accountStatus)
                .name("account1")
                .ledgerEntity(ledgerEntity).build();
        accountRepository.save(account);
        return account;
    }

    private Transaction prepareTransaction(long amount, Wallet fromWallet, Wallet toWallet, TransactionStatus transactionStatus) {
//        walletRepository.save(fromWallet);
//        walletRepository.save(toWallet);

        Transaction transaction = Transaction.builder()
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(BigDecimal.valueOf(amount))
                .status(transactionStatus)
                .build();

        return transactionRepository.save(transaction);
    }

    private Wallet prepareWallet(Account account, String name, AssetType assetType) {
        Wallet wallet = Wallet.builder()
                .account(account)
                .name(name)
                .balance(BigDecimal.valueOf(1000L))
                .assetType(assetType)
                .build();
        walletRepository.save(wallet);
        return wallet;
    }
}