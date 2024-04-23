package com.assignment.ledger.account.adapter.web;

import com.assignment.ledger.account.application.dto.ChangeAccountStatusRequest;
import com.assignment.ledger.account.application.ports.in.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController implements AccountApi {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    /**
     * change account to new status
     *
     * @param id account id
     * @param changeAccountStatusRequest the new account status
     */
    @PostMapping("/{id}/status")
    public void changeAccountStatus(@PathVariable Long id,
                                    @Valid @RequestBody ChangeAccountStatusRequest changeAccountStatusRequest) {
        accountService.changeAccountStatus(id, changeAccountStatusRequest);
    }
}
