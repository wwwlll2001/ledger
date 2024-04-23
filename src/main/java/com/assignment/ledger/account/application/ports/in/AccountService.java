package com.assignment.ledger.account.application.ports.in;

import com.assignment.ledger.account.application.dto.ChangeAccountStatusRequest;

public interface AccountService {

    /**
     * change account to new status after validation
     *
     * @param id
     * @param changeAccountStatusRequest
     */
    void changeAccountStatus(Long id, ChangeAccountStatusRequest changeAccountStatusRequest);
}
