package com.assignment.ledger.transaction.application.dto;

import com.assignment.ledger.common.exception.ErrorCode;
import com.assignment.ledger.transaction.domain.Transaction;
import com.assignment.ledger.transaction.domain.TransactionStatus;
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
public class TransactionDto {

    private Long id;
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
    private ErrorCode errorCode;
    private String failedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionDto from(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .fromWalletId(transaction.getFromWallet().getId())
                .toWalletId(transaction.getToWallet().getId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .errorCode(transaction.getErrorCode())
                .failedReason(transaction.getFailedReason())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
