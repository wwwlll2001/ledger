package com.assignment.ledger.transaction.adapter.web;

import com.assignment.ledger.ApiTestBase;
import com.assignment.ledger.account.application.ports.out.AccountRepository;
import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.entity.application.ports.out.LedgerEntityRepository;
import com.assignment.ledger.entity.domain.LedgerEntity;
import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.dto.UpdateTransactionRequest;
import com.assignment.ledger.transaction.application.ports.out.TransactionRepository;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.AssetType;
import com.assignment.ledger.wallet.domain.Wallet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

class TransactionControllerTest extends ApiTestBase {

    @Autowired
    LedgerEntityRepository ledgerEntityRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ApplicationContext context;

    @Test
    void should_start_one_transaction_successfully() throws IOException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from");
        Wallet toWallet = prepareWallet(account, "to");

        StartTransactionRequest startTransactionRequest = StartTransactionRequest.builder()
                                                     .fromWalletId(fromWallet.getId())
                                                     .toWalletId(toWallet.getId())
                                                     .amount(BigDecimal.valueOf(100L))
                                                     .build();

        given()
                .contentType("application/json")
                .when()
                .body(getObjectMapper().writeValueAsString(singletonList(startTransactionRequest)))
                .post("transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value());

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions.size(), is(1));
        assertThat(transactions.get(0).getStatus(), is(TransactionStatus.PROCESSING));


        List<TransactionDto> transactionDtos = receiveTransactionDtos();
        assertThat(transactionDtos.get(0).getId(), is(1L));
    }

    @Test
    void should_start_two_transaction_successfully() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from");
        Wallet toWallet = prepareWallet(account, "to");

        StartTransactionRequest startTransactionRequest1 = StartTransactionRequest.builder()
                .fromWalletId(fromWallet.getId())
                .toWalletId(toWallet.getId())
                .amount(BigDecimal.valueOf(100L))
                .build();

        StartTransactionRequest startTransactionRequest2 = StartTransactionRequest.builder()
                .fromWalletId(fromWallet.getId())
                .toWalletId(toWallet.getId())
                .amount(BigDecimal.valueOf(100L))
                .build();

        given()
                .contentType("application/json")
                .when()
                .body(getObjectMapper().writeValueAsString(asList(startTransactionRequest1, startTransactionRequest2)))
                .post("transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("transactions", hasSize(2))
                .body("transactions.id", hasItems(1, 2));

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions.size(), is(2));
        assertThat(transactions.get(0).getStatus(), is(TransactionStatus.PROCESSING));

        List<TransactionDto> transactionDtos = receiveTransactionDtos();
        assertThat(transactionDtos.get(0).getId(), is(1L));
        assertThat(transactionDtos.size(), is(2));
    }

    @Test
    void should_start_update_transaction_successfully() throws IOException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Transaction transaction = prepareTransaction(account);

        UpdateTransactionRequest updateTransactionRequest = UpdateTransactionRequest.builder()
                                                                    .fromWalletId(transaction.getFromWallet().getId())
                                                                    .toWalletId(transaction.getToWallet().getId())
                                                                    .amount(BigDecimal.valueOf(30L))
                                                                    .build();


        given()
                .contentType("application/json")
                .when()
                .body(getObjectMapper().writeValueAsString(updateTransactionRequest))
                .put("transactions/" + 1)
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        TransactionDto transactionDto = receiveTransactionDto();
        assertThat(transactionDto.getId(), is(1L));
        assertThat(transactionDto.getAmount(), comparesEqualTo(BigDecimal.valueOf(30L)));
    }
    private Wallet prepareWallet(Account account, String name) {
        Wallet wallet = Wallet.builder()
                .account(account)
                .name(name)
                .balance(BigDecimal.valueOf(1000L))
                .assetType(AssetType
                        .FLAT_CURRENCY)
                .build();
        walletRepository.save(wallet);
        return wallet;
    }

    private Transaction prepareTransaction(Account account) {
        Wallet fromWallet = Wallet.builder()
                .account(account)
                .name("from")
                .balance(BigDecimal.valueOf(1000L))
                .assetType(AssetType
                        .FLAT_CURRENCY)
                .build();
        Wallet toWallet = Wallet.builder()
                .account(account)
                .name("to")
                .balance(BigDecimal.valueOf(1000L))
                .assetType(AssetType
                        .FLAT_CURRENCY)
                .build();
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        Transaction transaction = Transaction.builder()
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(BigDecimal.valueOf(500L))
                .status(TransactionStatus.PROCESSING)
                .build();
        return transactionRepository.save(transaction);
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

    private List<TransactionDto> receiveTransactionDtos() throws com.fasterxml.jackson.core.JsonProcessingException {
        OutputDestination outputDestination = context.getBean(OutputDestination.class);
        byte[] messagePayload = outputDestination.receive(10000, "processTransaction-out-0").getPayload();
        String message = new String(messagePayload);
        ObjectMapper objectMapper = getObjectMapper();
        List<TransactionDto> transactionDtos = objectMapper.readValue(message, new TypeReference<>() {});
        return transactionDtos;
    }

    private TransactionDto receiveTransactionDto() throws com.fasterxml.jackson.core.JsonProcessingException {
        OutputDestination outputDestination = context.getBean(OutputDestination.class);
        byte[] messagePayload = outputDestination.receive(10000, "processTransaction-out-0").getPayload();
        String message = new String(messagePayload);
        ObjectMapper objectMapper = getObjectMapper();
        TransactionDto transactionDto = objectMapper.readValue(message, TransactionDto.class);
        return transactionDto;
    }
}