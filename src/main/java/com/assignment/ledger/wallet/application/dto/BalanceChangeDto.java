package com.assignment.ledger.wallet.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceChangeDto {

    private Long id;
    private BigDecimal balanceChange;
    private BigDecimal newBalance;
    private Long transactionId;
    private LocalDateTime changeTime;
}
