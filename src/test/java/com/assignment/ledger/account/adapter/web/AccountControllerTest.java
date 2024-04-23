package com.assignment.ledger.account.adapter.web;

import com.assignment.ledger.ApiTestBase;
import com.assignment.ledger.account.application.dto.ChangeAccountStatusRequest;
import com.assignment.ledger.account.application.ports.out.AccountRepository;
import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.common.exception.ErrorCode;
import com.assignment.ledger.entity.application.ports.out.LedgerEntityRepository;
import com.assignment.ledger.entity.domain.LedgerEntity;
import com.assignment.ledger.transaction.application.ports.out.TransactionRepository;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.AssetType;
import com.assignment.ledger.wallet.domain.Wallet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class AccountControllerTest extends ApiTestBase {

    @Autowired
    LedgerEntityRepository ledgerEntityRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Test
    void should_open_account_successfully_when_account_status_is_closed() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {

        Account account = prepareAccount(AccountStatus.CLOSE);
        ChangeAccountStatusRequest changeAccountStatusRequest =
                ChangeAccountStatusRequest.builder().newStatus(AccountStatus.OPEN).build();

        given()
                .contentType("application/json")
        .when()
                .body(getObjectMapper().writeValueAsString(changeAccountStatusRequest))
                .post("accounts/1/status")
        .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Account updatedAccount = accountRepository.findById(account.getId()).orElse(null);
        assertThat(updatedAccount.getStatus(), is(AccountStatus.OPEN));
    }

    @Test
    void should_close_account_successfully_when_account_status_is_opened() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {

        Account account = prepareAccount(AccountStatus.OPEN);
        ChangeAccountStatusRequest changeAccountStatusRequest =
                ChangeAccountStatusRequest.builder().newStatus(AccountStatus.CLOSE).build();

        given()
                .contentType("application/json")
        .when()
                .body(getObjectMapper().writeValueAsString(changeAccountStatusRequest))
                .post("accounts/1/status")
        .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Account updatedAccount = accountRepository.findById(account.getId()).orElse(null);
        assertThat(updatedAccount.getStatus(), is(AccountStatus.CLOSE));
    }

    @Test
    void should_open_account_failed_when_account_status_is_opened() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {

        prepareAccount(AccountStatus.OPEN);
        ChangeAccountStatusRequest changeAccountStatusRequest =
                ChangeAccountStatusRequest.builder().newStatus(AccountStatus.OPEN).build();

        given()
                .contentType("application/json")
        .when()
                .body(getObjectMapper().writeValueAsString(changeAccountStatusRequest))
                .post("accounts/1/status")
        .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errorCode", is(ErrorCode.INVALID_ACCOUNT_STATUS.name()));
    }

    @Test
    void should_open_account_failed_when_account_not_exist() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {

        prepareAccount(AccountStatus.OPEN);
        ChangeAccountStatusRequest changeAccountStatusRequest =
                ChangeAccountStatusRequest.builder().newStatus(AccountStatus.OPEN).build();

        given()
                .contentType("application/json")
        .when()
                .body(getObjectMapper().writeValueAsString(changeAccountStatusRequest))
                .post("accounts/2/status")
        .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("errorCode", is(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void should_close_account_failed_when_processing_transaction_exist() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        prepareTransaction(account);
        ChangeAccountStatusRequest changeAccountStatusRequest =
                ChangeAccountStatusRequest.builder().newStatus(AccountStatus.CLOSE).build();

        given()
                .contentType("application/json")
                .when()
                .body(getObjectMapper().writeValueAsString(changeAccountStatusRequest))
                .post("accounts/1/status")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errorCode", is(ErrorCode.PROCESSING_TRANSACTION_EXIST.name()));
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

    private void prepareTransaction(Account account) {
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
        transactionRepository.save(transaction);
    }
}