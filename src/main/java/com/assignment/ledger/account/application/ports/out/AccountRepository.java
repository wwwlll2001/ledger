package com.assignment.ledger.account.application.ports.out;

import com.assignment.ledger.account.domain.Account;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Here, JpaRepository is leveraged as the outbound port for simplified purpose, actually, a technology neutral
 * interface should be declared as the outbound port to decouple the port interface and its concrete implementation
 *
 * */
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query(nativeQuery = true, name = "Account.countTransactions")
    Long countTransactions(@Param("id") Long accountId,
                           @Param("transaction_status") TransactionStatus transactionStatus);
}
