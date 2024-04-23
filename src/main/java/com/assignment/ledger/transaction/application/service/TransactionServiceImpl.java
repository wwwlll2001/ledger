package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.common.exception.ErrorCode;
import com.assignment.ledger.common.exception.InSufficientFundsException;
import com.assignment.ledger.common.exception.InvalidAccountStatusException;
import com.assignment.ledger.common.exception.InvalidAmountException;
import com.assignment.ledger.common.exception.InvalidTransactionStatusException;
import com.assignment.ledger.common.exception.ResourceNotFoundException;
import com.assignment.ledger.common.exception.WalletTypeNotConsistentException;
import com.assignment.ledger.transaction.adapter.message.TransactionMessageType;
import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.dto.StartTransactionResponse;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.dto.UpdateTransactionRequest;
import com.assignment.ledger.transaction.application.ports.in.TransactionService;
import com.assignment.ledger.transaction.application.ports.out.TransactionRepository;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import com.assignment.ledger.wallet.application.dto.BalanceChangeDto;
import com.assignment.ledger.wallet.application.ports.out.WalletRepository;
import com.assignment.ledger.wallet.domain.Wallet;
import com.assignment.ledger.wallet.domain.WalletMessageType;
import io.cloudevents.CloudEvent;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.assignment.ledger.common.Constant.PUBLIC_TRANSACTION_OUT_BINDING;
import static com.assignment.ledger.common.Constant.TRANSACTION_OUT_BINDING;
import static com.assignment.ledger.common.Constant.WALLET_OUT_BINDING;
import static com.assignment.ledger.common.message.MessageHelper.buildCloudEvent;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final WalletRepository walletRepository;

    private final StreamBridge streamBridge;

    @Lazy
    @Autowired
    private TransactionServiceImpl self;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  WalletRepository walletRepository, StreamBridge streamBridge) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.streamBridge = streamBridge;
    }

    /**
     * Start transaction, save transactions first, and process transactions asynchronously, assume transaction in batch
     * transactions will not impact other transaction in the same batch, i.e. one transaction failed will not cause
     * other transaction failed in the same batch
     *
     * @param startTransactionRequests start transaction requests
     * @return the transaction data with id so that client could get the transaction result via the transaction id.
     */
    @Transactional
    @Override
    public StartTransactionResponse startTransaction(List<StartTransactionRequest> startTransactionRequests) {
        List<TransactionDto> transactionDtos = new ArrayList<>();
        startTransactionRequests.forEach(startTransactionRequest -> {
            Transaction transaction = startProcessingTransaction(startTransactionRequest);
            transactionDtos.add(TransactionDto.from(transaction));
        });
        //send transactions
        streamBridge.send(TRANSACTION_OUT_BINDING,
                MessageBuilder.withPayload(
                                          buildCloudEvent(transactionDtos, String.valueOf(TransactionMessageType.INIT)))
                            .build());
        return StartTransactionResponse.builder().transactions(transactionDtos).build();
    }

    @Override
    @Transactional
    public void processTransaction(TransactionDto transactionDto) {
        Transaction transaction;
        try {
            transaction = handleTransaction(transactionDto);

            sendTransactionCompletedEvent(transaction);

            if (TransactionStatus.CLEARED.equals(transaction.getStatus())) {
                calculateAndSendBalanceChange(transaction, true);
                calculateAndSendBalanceChange(transaction, false);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //if transaction failed, mark the transaction failed in db and send the failed message to client.
            self.handleProcessTransactionException(transactionDto, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProcessTransactionException(TransactionDto transactionDto, Exception e) {
        Transaction transaction = transactionRepository.findById(transactionDto.getId()).orElse(null);

        if (e instanceof InvalidAmountException) {
            transaction.setErrorCode(ErrorCode.INVALID_AMOUNT);
        } else if (e instanceof InvalidAccountStatusException) {
            transaction.setErrorCode(ErrorCode.INVALID_ACCOUNT_STATUS);
        } else if (e instanceof WalletTypeNotConsistentException) {
            transaction.setErrorCode(ErrorCode.WALLET_TYPE_NOT_CONSISTENT);
        } else if (e instanceof InSufficientFundsException) {
            transaction.setErrorCode(ErrorCode.INSUFFICIENT_FUNDS);
        } else if (e instanceof OptimisticLockException || e instanceof ObjectOptimisticLockingFailureException) {
            transaction.setErrorCode(ErrorCode.CONCURRENT_OPERATION);
        }
        transaction.setFailedReason(e.getMessage());
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setUpdatedAt(LocalDateTime.now());

        CloudEvent payload = buildCloudEvent(TransactionDto.from(transaction),
                                             String.valueOf(TransactionMessageType.COMPLETED));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload)
                                                    .setHeader(KafkaHeaders.KEY, transactionDto.getId().toString())
                                                    .build();
        streamBridge.send(TRANSACTION_OUT_BINDING, message);
        streamBridge.send(PUBLIC_TRANSACTION_OUT_BINDING, message);
    }

    @Override
    @Transactional
    public void updateTransaction(TransactionDto transactionDto) {
        Transaction transaction;
        try {
            transaction = transactionRepository.findById(transactionDto.getId()).orElse(null);
            List<BalanceChangeDto> balanceChangeDtos = modifyTransaction(transaction,
                                                                         transactionDto.getAmount(),
                                                                         transactionDto.getFromWalletId(),
                                                                         transactionDto.getToWalletId());

            //send transaction event
            sendUpdateTransactionEvent(transaction);

            //send balance change event
            balanceChangeDtos.forEach(this::sendBalanceChangeDto);
        } catch (InvalidTransactionStatusException | ResourceNotFoundException
                 | InSufficientFundsException | OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            //if update failed, just send the update failed result to client, no need to modify current transaction data
            self.handleUpdateTransactionException(transactionDto, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUpdateTransactionException(TransactionDto transactionDto, RuntimeException e) {
        if (e instanceof InvalidTransactionStatusException) {
            transactionDto.setErrorCode(ErrorCode.INVALID_TRANSACTION_STATUS);
        } else if (e instanceof ResourceNotFoundException) {
            transactionDto.setErrorCode(ErrorCode.RESOURCE_NOT_FOUND);
        } else if (e instanceof InSufficientFundsException) {
            transactionDto.setErrorCode(ErrorCode.INSUFFICIENT_FUNDS);
        } else {
            transactionDto.setErrorCode(ErrorCode.CONCURRENT_OPERATION);
        }
        transactionDto.setFailedReason(e.getMessage());
        transactionDto.setUpdatedAt(LocalDateTime.now());

        CloudEvent payload = buildCloudEvent(transactionDto, String.valueOf(TransactionMessageType.UPDATE_FAILED));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.KEY, transactionDto.getId().toString())
                .build();
        streamBridge.send(TRANSACTION_OUT_BINDING, message);
        streamBridge.send(PUBLIC_TRANSACTION_OUT_BINDING, message);
    }

    private List<BalanceChangeDto> modifyTransaction(Transaction transaction,
                                                    BigDecimal newAmount,
                                                    Long newFromWalletId,
                                                    Long newToWalletId) {

        if (transaction.getStatus() != TransactionStatus.CLEARED) {
            throw new IllegalStateException("Only completed transactions can be modified");
        }

        Wallet oldFromWallet = transaction.getFromWallet();
        Wallet oldToWallet = transaction.getToWallet();
        Wallet newFromWallet = walletRepository.findById(newFromWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("New from-wallet not found"));
        Wallet newToWallet = walletRepository.findById(newToWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("New to-wallet not found"));

        BigDecimal oldFromWalletOriginalBalance = oldFromWallet.getBalance();
        BigDecimal oldToWalletOriginalBalance = oldToWallet.getBalance();
        BigDecimal newFromWalletOriginalBalance = newFromWallet.getBalance();
        BigDecimal newToWalletOriginalBalance = newToWallet.getBalance();

        // Adjusting balances as per new transaction details
        adjustWalletBalancesForTransaction(oldFromWallet,
                                           oldToWallet,
                                           newFromWallet,
                                           newToWallet,
                                           transaction.getAmount(),
                                           newAmount);

        transaction.setFromWallet(newFromWallet);
        transaction.setToWallet(newToWallet);
        transaction.setAmount(newAmount);
        transaction.setUpdatedAt(LocalDateTime.now());

        // calculate balance change
        return buildBalanceChangeDtos(transaction,
                                      oldFromWallet,
                                      oldToWallet,
                                      newFromWallet,
                                      newToWallet,
                                      oldFromWalletOriginalBalance,
                                      oldToWalletOriginalBalance,
                                      newFromWalletOriginalBalance,
                                      newToWalletOriginalBalance);
    }

    private BalanceChangeDto buildBalanceChangeDto(Wallet wallet,
                                                          Transaction transaction,
                                                          BigDecimal originalBalance) {
        return BalanceChangeDto.builder().id(wallet.getId())
                .transactionId(transaction.getId())
                .newBalance(wallet.getBalance())
                .balanceChange(
                        wallet.getBalance().subtract(originalBalance))
                .changeTime(transaction.getUpdatedAt())
                .build();
    }

    private void adjustWalletBalancesForTransaction(Wallet oldFromWallet,
                                                    Wallet oldToWallet,
                                                    Wallet newFromWallet,
                                                    Wallet newToWallet,
                                                    BigDecimal originalAmount,
                                                    BigDecimal newAmount) {
        oldFromWallet.setBalance(oldFromWallet.getBalance().add(originalAmount));
        oldToWallet.setBalance(oldToWallet.getBalance().subtract(originalAmount));

        if (oldToWallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InSufficientFundsException("Old to-wallet balance cannot go negative.");
        }

        BigDecimal potentialNewFromBalance = newFromWallet.getBalance().subtract(newAmount);
        if (potentialNewFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InSufficientFundsException("Insufficient funds in the new from-wallet for this transaction");
        }
        newFromWallet.setBalance(potentialNewFromBalance);

        newToWallet.setBalance(newToWallet.getBalance().add(newAmount));
    }

    private void sendUpdateTransactionEvent(Transaction transaction) {
        TransactionDto newTransactionDto = TransactionDto.from(transaction);
        CloudEvent payload = buildCloudEvent(newTransactionDto, String.valueOf(TransactionMessageType.UPDATE_SUCCESS));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.KEY, newTransactionDto.getId().toString())
                .build();
        streamBridge.send(TRANSACTION_OUT_BINDING, message);
    }

    @Override
    public void startUpdateTransaction(Long id, UpdateTransactionRequest updateTransactionRequest) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() ->
                                                                new ResourceNotFoundException("transaction not exist"));

        sendTransactionUpdateEvent(transaction, updateTransactionRequest);
    }

    private void calculateAndSendBalanceChange(Transaction transaction, boolean debit) {
        Wallet wallet;
        BigDecimal balanceChange;
        if (debit) {
            wallet = transaction.getFromWallet();
            balanceChange = transaction.getAmount().multiply(BigDecimal.valueOf(-1L));
        } else {
            wallet = transaction.getToWallet();
            balanceChange = transaction.getAmount();
        }
        BalanceChangeDto balanceChangeDto = BalanceChangeDto.builder().id(wallet.getId())
                                                                      .balanceChange(balanceChange)
                                                                      .newBalance(wallet.getBalance())
                                                                      .transactionId(transaction.getId())
                                                                      .changeTime(transaction.getUpdatedAt())
                                                                      .build();

        sendBalanceChangeDto(balanceChangeDto);
    }

    private void sendBalanceChangeDto(BalanceChangeDto balanceChangeDto) {
        CloudEvent payload = buildCloudEvent(balanceChangeDto, String.valueOf(WalletMessageType.BALANCE_CHANGE));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload).setHeader(KafkaHeaders.KEY,
                balanceChangeDto.getId().toString()).build();
        streamBridge.send(WALLET_OUT_BINDING, message);
    }

    private void sendTransactionCompletedEvent(Transaction transaction) {
        CloudEvent payload =
                    buildCloudEvent(TransactionDto.from(transaction), String.valueOf(TransactionMessageType.COMPLETED));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload).setHeader(KafkaHeaders.KEY,
                                                                                transaction.getId().toString()).build();
        streamBridge.send(TRANSACTION_OUT_BINDING, message);
        streamBridge.send(PUBLIC_TRANSACTION_OUT_BINDING, message);
    }

    @NotNull
    private Transaction handleTransaction(TransactionDto transactionDto) {
        Transaction transaction = transactionRepository.findById(transactionDto.getId()).orElse(null);
        validateAccountStatus(transaction);
        validateWalletType(transaction);

        Wallet fromWallet = transaction.getFromWallet();
        Wallet toWallet = transaction.getToWallet();

        fromWallet.debit(transaction.getAmount());
        toWallet.credit(transaction.getAmount());

        transaction.setStatus(TransactionStatus.CLEARED);
        transaction.setUpdatedAt(LocalDateTime.now());

        return transaction;
    }

    private Transaction startProcessingTransaction(StartTransactionRequest startTransactionRequest) {
        Wallet fromWallet = walletRepository.findById(startTransactionRequest.getFromWalletId()).orElse(null);
        Wallet toWallet = walletRepository.findById(startTransactionRequest.getToWalletId()).orElse(null);
        Transaction transaction = Transaction.builder()
                                             .fromWallet(fromWallet)
                                             .toWallet(toWallet)
                                             .amount(startTransactionRequest.getAmount())
                                             .createdAt(LocalDateTime.now())
                                             .updatedAt(LocalDateTime.now())
                                             .status(TransactionStatus.PROCESSING).build();
        transactionRepository.save(transaction);
        return transaction;
    }

    private void validateWalletType(Transaction transaction) {
        if (!transaction.getFromWallet().getAssetType().equals(transaction.getToWallet().getAssetType())) {
            throw new WalletTypeNotConsistentException("wallets type not consistent");
        }
    }

    private void validateAccountStatus(Transaction transaction) {
        if (AccountStatus.CLOSE.equals(transaction.getFromWallet().getAccount().getStatus())) {
            throw new InvalidAccountStatusException("from account is closed");
        } else if (AccountStatus.CLOSE.equals(transaction.getToWallet().getAccount().getStatus())) {
            throw new InvalidAccountStatusException("to account is closed");
        }
    }

    private void sendTransactionUpdateEvent(Transaction transaction,
                                            UpdateTransactionRequest updateTransactionRequest) {
        TransactionDto transactionDto = TransactionDto.builder()
                                                      .id(transaction.getId())
                                                      .fromWalletId(updateTransactionRequest.getFromWalletId())
                                                      .toWalletId(updateTransactionRequest.getToWalletId())
                                                      .amount(updateTransactionRequest.getAmount())
                                                      .updatedAt(LocalDateTime.now())
                                                      .build();
        CloudEvent payload = buildCloudEvent(transactionDto, String.valueOf(TransactionMessageType.UPDATE));
        Message<CloudEvent> message = MessageBuilder.withPayload(payload).setHeader(KafkaHeaders.KEY,
                transaction.getId().toString()).build();
        streamBridge.send(TRANSACTION_OUT_BINDING, message);
    }

    @NotNull
    private List<BalanceChangeDto> buildBalanceChangeDtos(Transaction transaction,
                                                          Wallet oldFromWallet,
                                                          Wallet oldToWallet,
                                                          Wallet newFromWallet,
                                                          Wallet newToWallet,
                                                          BigDecimal oldFromWalletOriginalBalance,
                                                          BigDecimal oldToWalletOriginalBalance,
                                                          BigDecimal newFromWalletOriginalBalance,
                                                          BigDecimal newToWalletOriginalBalance) {
        //consider there might be duplications among the old from wallet, the old to wallet, the new from wallet and the
        // new to wallet, we need to guarantee that one wallet should only receive one balance change message.
        Set<Long> walletIds = new HashSet<>();
        walletIds.add(oldFromWallet.getId());
        walletIds.add(oldToWallet.getId());
        walletIds.add(newFromWallet.getId());
        walletIds.add(newToWallet.getId());

        List<BalanceChangeDto> balanceChanges = new ArrayList<>();
        walletIds.forEach(id -> {
            if (id.equals(oldFromWallet.getId())) {
                balanceChanges.add(buildBalanceChangeDto(oldFromWallet, transaction, oldFromWalletOriginalBalance));
            } else if (id.equals(oldToWallet.getId())) {
                balanceChanges.add(buildBalanceChangeDto(oldToWallet, transaction, oldToWalletOriginalBalance));
            } else if (id.equals(newFromWallet.getId())) {
                balanceChanges.add(buildBalanceChangeDto(newFromWallet, transaction, newFromWalletOriginalBalance));
            } else if (id.equals(newToWallet.getId())) {
                balanceChanges.add(buildBalanceChangeDto(newToWallet, transaction, newToWalletOriginalBalance));
            }
        });
        return balanceChanges;
    }
}
