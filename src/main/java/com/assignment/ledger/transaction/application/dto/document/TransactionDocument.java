package com.assignment.ledger.transaction.application.dto.document;

import com.assignment.ledger.common.exception.ErrorCode;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "transactions")
public class TransactionDocument {

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

    @Field(type = FieldType.Date,
            format = DateFormat.date_hour_minute_second_millis, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime transactionTime;

    public static TransactionDocument from(TransactionDto transactionDto, String fromWalletName, String toWalletName) {
        return TransactionDocument.builder()
                                  .id(transactionDto.getId())
                                  .amount(transactionDto.getAmount())
                                  .fromWalletId(transactionDto.getFromWalletId())
                                  .fromWalletName(fromWalletName)
                                  .toWalletId(transactionDto.getToWalletId())
                                  .toWalletName(toWalletName)
                                  .status(transactionDto.getStatus())
                                  .errorCode(transactionDto.getErrorCode())
                                  .failedReason(transactionDto.getFailedReason())
                                  .transactionTime(transactionDto.getUpdatedAt())
                                  .build();
    }
}
