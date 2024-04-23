package com.assignment.ledger.account.application.service;


import com.assignment.ledger.account.application.dto.ChangeAccountStatusRequest;
import com.assignment.ledger.account.application.ports.in.AccountService;
import com.assignment.ledger.account.application.ports.out.AccountRepository;
import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.account.domain.AccountStatus;
import com.assignment.ledger.common.exception.InvalidAccountStatusException;
import com.assignment.ledger.common.exception.ProcessingTransactionExistException;
import com.assignment.ledger.common.exception.ResourceNotFoundException;
import com.assignment.ledger.transaction.domain.TransactionStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void changeAccountStatus(Long id, ChangeAccountStatusRequest changeAccountStatusRequest) {
        Account account = accountRepository
                                      .findById(id)
                                      .orElseThrow(() -> new ResourceNotFoundException("Account not exist"));
        validateAccountStatus(changeAccountStatusRequest.getNewStatus(), account);
        account.setStatus(changeAccountStatusRequest.getNewStatus());
    }

    private void validateAccountStatus(AccountStatus newStatus, Account account) {
        if (account.getStatus().equals(newStatus)) {
            throw new InvalidAccountStatusException("invalid account status");
        }

        // if processing transaction exists, account could not be closed
        if (AccountStatus.CLOSE.equals(newStatus)
                && accountRepository.countTransactions(account.getId(), TransactionStatus.PROCESSING) != 0) {
            throw new ProcessingTransactionExistException("processing transaction exist, account could not be closed");
        }
    }
}
