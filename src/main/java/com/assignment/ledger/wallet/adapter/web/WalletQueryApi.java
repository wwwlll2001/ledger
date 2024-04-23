package com.assignment.ledger.wallet.adapter.web;

import com.assignment.ledger.common.exception.ErrorResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Tag(name = "Wallet Queries", description = "Transaction query apis.")
public interface WalletQueryApi {

    @Operation(summary = "Retrieve wallet balance at specific timestamp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieve successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "500", description = "Internal system error.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class)))
    })
    Mono<BigDecimal> getWalletBalanceAt(@PathVariable("id") Long walletId, @RequestParam LocalDateTime queryTime);
}
