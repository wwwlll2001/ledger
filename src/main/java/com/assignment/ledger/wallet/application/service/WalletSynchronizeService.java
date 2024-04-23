package com.assignment.ledger.wallet.application.service;

import com.assignment.ledger.wallet.application.dto.BalanceChangeDto;

public interface WalletSynchronizeService {


    /**
     * Synchronize balance change data to elasticsearch.
     *
     * @param balanceChangeDto balance change data
     */
    void synchronizeBalanceChange(BalanceChangeDto balanceChangeDto);
}
