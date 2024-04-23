package com.assignment.ledger.wallet.application.dto;

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
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "wallet_change_history")
public class WalletBalanceHistoryDocument {

    @Id
    private String id;
    private Long walletId;
    private BigDecimal balance;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis,
                                                                                 pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS ")
    private LocalDateTime changeTime;

    public static WalletBalanceHistoryDocument from(BalanceChangeDto balanceChangeDto) {
        return WalletBalanceHistoryDocument.builder().id(UUID.randomUUID().toString())
                                                     .balance(balanceChangeDto.getNewBalance())
                                                     .walletId(balanceChangeDto.getId())
                                                     .changeTime(balanceChangeDto.getChangeTime())
                                                     .build();
    }
}
