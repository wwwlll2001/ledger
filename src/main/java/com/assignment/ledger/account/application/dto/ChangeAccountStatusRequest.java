package com.assignment.ledger.account.application.dto;

import com.assignment.ledger.account.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeAccountStatusRequest {

    @NotNull
    AccountStatus newStatus;
}
