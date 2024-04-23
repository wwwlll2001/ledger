package com.assignment.ledger.transaction.application.dto;

import com.assignment.ledger.common.exception.ErrorCode;
import com.assignment.ledger.transaction.application.dto.document.TransactionDocument;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListDto {

    @Id
    private Long id;
    private BigDecimal amount;
    private Long fromWalletId;
    private String fromWalletName;
    private Long toWalletId;
    private String toWalletName;
    private TransactionStatus status;
    private ErrorCode errorCode;
    private String failedReason;
    private LocalDateTime transactionTime;

    public static TransactionListDto from(TransactionDocument transactionDocument) {
        return TransactionListDto.builder()
                                  .id(transactionDocument.getId())
                                  .amount(transactionDocument.getAmount())
                                  .fromWalletId(transactionDocument.getFromWalletId())
                                  .fromWalletName(transactionDocument.getFromWalletName())
                                  .toWalletId(transactionDocument.getToWalletId())
                                  .toWalletName(transactionDocument.getToWalletName())
                                  .status(transactionDocument.getStatus())
                                  .errorCode(transactionDocument.getErrorCode())
                                  .failedReason(transactionDocument.getFailedReason())
                                  .transactionTime(transactionDocument.getTransactionTime())
                                  .build();

    }
}
