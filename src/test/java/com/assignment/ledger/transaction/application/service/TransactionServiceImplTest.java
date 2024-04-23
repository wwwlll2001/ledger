package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.ApiTestBase;
import com.assignment.ledger.account.application.ports.out.AccountRepository;
import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.common.exception.*;
import com.assignment.ledger.entity.application.ports.out.LedgerEntityRepository;
import com.assignment.ledger.entity.domain.LedgerEntity;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.ports.out.TransactionRepository;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import com.assignment.ledger.wallet.application.dto.BalanceChangeDto;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.AssetType;
import com.assignment.ledger.wallet.domain.Wallet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class TransactionServiceImplTest extends ApiTestBase {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private LedgerEntityRepository ledgerEntityRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    ApplicationContext context;

    @Test
    public void should_process_transaction_successfully() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        Transaction transaction = prepareTransaction(500L, fromWallet, toWallet, TransactionStatus.PROCESSING);
        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId()).build();
        transactionService.processTransaction(transactionDto);

        //assert db
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.CLEARED));

        assertThat(updatedTransaction.getFromWallet().getBalance(), comparesEqualTo(BigDecimal.valueOf(500L)));
        assertThat(updatedTransaction.getToWallet().getBalance(), comparesEqualTo(BigDecimal.valueOf(1500L)));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.CLEARED));

        //assert message
        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedPublicTransactionDto.getStatus(), is(TransactionStatus.CLEARED));

        BalanceChangeDto fromBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(fromBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(-500L)));
        assertThat(fromBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(500L)));

        BalanceChangeDto toBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(toBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(500L)));
        assertThat(toBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(1500L)));
    }

    @Test
    public void should_process_transaction_failed_when_account_is_invalid() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.CLOSE);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        Transaction transaction = prepareTransaction(500L, fromWallet, toWallet, TransactionStatus.PROCESSING);

        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId()).build();
//        try {
//            transactionService.processTransaction(transactionDto);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        assertThrows(InvalidAccountStatusException.class, () -> {
            // 模拟数据库操作失败的情况
            transactionService.processTransaction(transactionDto);
        });

        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.FAILED));
        assertThat(updatedTransaction.getErrorCode(), is(ErrorCode.INVALID_ACCOUNT_STATUS));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.FAILED));

        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedPublicTransactionDto.getStatus(), is(TransactionStatus.FAILED));
    }

    @Test
    public void should_process_transaction_failed_when_amount_is_invalid() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        Transaction transaction = prepareTransaction(-500L, fromWallet, toWallet, TransactionStatus.PROCESSING);

        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId()).build();
        assertThrows(InvalidAmountException.class, () -> {
            // 模拟数据库操作失败的情况
            transactionService.processTransaction(transactionDto);
        });

        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.FAILED));
        assertThat(updatedTransaction.getErrorCode(), is(ErrorCode.INVALID_AMOUNT));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.FAILED));

        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedPublicTransactionDto.getStatus(), is(TransactionStatus.FAILED));

    }

    @Test
    public void should_process_transaction_failed_when_fund_is_insufficient() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        Transaction transaction = prepareTransaction(10500L, fromWallet, toWallet, TransactionStatus.PROCESSING);
        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId()).build();
        assertThrows(InSufficientFundsException.class, () -> {
            // 模拟数据库操作失败的情况
            transactionService.processTransaction(transactionDto);
        });

        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.FAILED));
        assertThat(updatedTransaction.getErrorCode(), is(ErrorCode.INSUFFICIENT_FUNDS));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.FAILED));

        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedPublicTransactionDto.getStatus(), is(TransactionStatus.FAILED));
    }

    @Test
    public void should_process_transaction_failed_when_wallet_type_is_not_consistent() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet fromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet toWallet = prepareWallet(account, "to", AssetType
                .BOND);
        Transaction transaction = prepareTransaction(500L, fromWallet, toWallet, TransactionStatus.PROCESSING);
        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId()).build();
        assertThrows(WalletTypeNotConsistentException.class, () -> {
            // 模拟数据库操作失败的情况
            transactionService.processTransaction(transactionDto);
        });

        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.FAILED));
        assertThat(updatedTransaction.getErrorCode(), is(ErrorCode.WALLET_TYPE_NOT_CONSISTENT));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.FAILED));

        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedPublicTransactionDto.getStatus(), is(TransactionStatus.FAILED));
    }

    @Test
    public void should_update_transaction_successfully() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet oldFromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet oldToWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);

        Wallet newFromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet newToWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        Transaction transaction = prepareTransaction(500L, oldFromWallet, oldToWallet, TransactionStatus.CLEARED);
        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId())
                                                                               .fromWalletId(newFromWallet.getId())
                                                                               .toWalletId(newToWallet.getId())
                                                                               .amount(BigDecimal.valueOf(300L))
                                                                               .build();
        transactionService.updateTransaction(transactionDto);

        //assert db
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.CLEARED));
        assertThat(updatedTransaction.getFromWallet().getId(), is(newFromWallet.getId()));
        assertThat(updatedTransaction.getToWallet().getId(), is(newToWallet.getId()));
        assertThat(updatedTransaction.getAmount(), comparesEqualTo(transactionDto.getAmount()));

        Wallet updatedNewFromWallet = walletRepository.findById(updatedTransaction.getFromWallet().getId()).orElse(null);
        assertThat(updatedNewFromWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(700L)));

        Wallet updatedNewToWallet = walletRepository.findById(updatedTransaction.getToWallet().getId()).orElse(null);
        assertThat(updatedNewToWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1300L)));

        Wallet updatedOldFromWallet = walletRepository.findById(oldFromWallet.getId()).orElse(null);
        assertThat(updatedOldFromWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1500)));

        Wallet updatedOldToWallet = walletRepository.findById(oldToWallet.getId()).orElse(null);
        assertThat(updatedOldToWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(500)));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getStatus(), is(TransactionStatus.CLEARED));

        BalanceChangeDto oldFromBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(oldFromBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(500L)));
        assertThat(oldFromBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(1500L)));

        BalanceChangeDto oldToBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(oldToBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(-500L)));
        assertThat(oldToBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(500L)));

        BalanceChangeDto newFromBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(newFromBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(-300)));
        assertThat(newFromBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(700L)));

        BalanceChangeDto newToBalanceChangeDto = receiveBalanceChangeDto();
        assertThat(newToBalanceChangeDto.getBalanceChange(), comparesEqualTo(BigDecimal.valueOf(300L)));
        assertThat(newToBalanceChangeDto.getNewBalance(), comparesEqualTo(BigDecimal.valueOf(1300L)));
    }

    @Test
    public void should_update_transaction_failed_when_insufficient_funds() throws JsonProcessingException {
        Account account = prepareAccount(AccountStatus.OPEN);
        Wallet oldFromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet oldToWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);

        Wallet newFromWallet = prepareWallet(account, "from", AssetType
                .FLAT_CURRENCY);
        Wallet newToWallet = prepareWallet(account, "to", AssetType
                .FLAT_CURRENCY);
        long oldAmount = 500L;
        Transaction transaction = prepareTransaction(oldAmount, oldFromWallet, oldToWallet, TransactionStatus.CLEARED);
        long newAmount = 30000L;
        TransactionDto transactionDto = TransactionDto.builder().id(transaction.getId())
                .fromWalletId(newFromWallet.getId())
                .toWalletId(newToWallet.getId())
                .amount(BigDecimal.valueOf(newAmount))
                .build();

        assertThrows(InSufficientFundsException.class, () -> {
            // 模拟数据库操作失败的情况
            transactionService.updateTransaction(transactionDto);
        });

        //assert db
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElse(null);
        assertThat(updatedTransaction.getStatus(), is(TransactionStatus.CLEARED));
        assertThat(updatedTransaction.getFromWallet().getId(), is(oldFromWallet.getId()));
        assertThat(updatedTransaction.getToWallet().getId(), is(oldToWallet.getId()));
        assertThat(updatedTransaction.getAmount(), comparesEqualTo(BigDecimal.valueOf(oldAmount)));

        Wallet updatedNewFromWallet = walletRepository.findById(newFromWallet.getId()).orElse(null);
        assertThat(updatedNewFromWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1000)));

        Wallet updatedNewToWallet = walletRepository.findById(newToWallet.getId()).orElse(null);
        assertThat(updatedNewToWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1000L)));

        Wallet updatedOldFromWallet = walletRepository.findById(oldFromWallet.getId()).orElse(null);
        assertThat(updatedOldFromWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1000)));

        Wallet updatedOldToWallet = walletRepository.findById(oldToWallet.getId()).orElse(null);
        assertThat(updatedOldToWallet.getBalance(), comparesEqualTo(BigDecimal.valueOf(1000)));

        //assert message
        TransactionDto receivedTransactionDto = receiveTransactionDto("processTransaction-out-0");

        assertThat(receivedTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getAmount(), comparesEqualTo(BigDecimal.valueOf(newAmount)));

        TransactionDto receivedPublicTransactionDto = receiveTransactionDto("processPublicTransaction-out-0");

        assertThat(receivedPublicTransactionDto.getId(), is(transaction.getId()));
        assertThat(receivedTransactionDto.getAmount(), comparesEqualTo(BigDecimal.valueOf(newAmount)));
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

    private TransactionDto receiveTransactionDto(String bindingName) throws JsonProcessingException {
        OutputDestination outputDestination = context.getBean(OutputDestination.class);
        byte[] messagePayload = outputDestination.receive(10000, bindingName).getPayload();
        String message = new String(messagePayload);
        System.out.println("receive message: " + message);
        ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.readValue(message, TransactionDto.class);
    }

    private BalanceChangeDto receiveBalanceChangeDto() throws JsonProcessingException {
        OutputDestination outputDestination = context.getBean(OutputDestination.class);
        byte[] messagePayload = outputDestination.receive(10000, "processWallet-out-0").getPayload();
        String message = new String(messagePayload);
        ObjectMapper objectMapper = getObjectMapper();
        BalanceChangeDto balanceChangeDto = objectMapper.readValue(message, BalanceChangeDto.class);
        return balanceChangeDto;
    }
}