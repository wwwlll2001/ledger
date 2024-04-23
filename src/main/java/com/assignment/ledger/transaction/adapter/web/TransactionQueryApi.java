package com.assignment.ledger.transaction.adapter.web;

import com.assignment.ledger.common.exception.ErrorResult;
import com.assignment.ledger.transaction.application.dto.TransactionListDto;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

@Tag(name = "Transaction Queries", description = "Transaction query apis.")
public interface TransactionQueryApi {

    @Operation(summary = "Retrieve transactions conditionally.", description = """
            Retrieve history transactions.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieve successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionListDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal system error.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class)))
    })
    Flux<TransactionListDto> retrieveTransactions(
                                                  @RequestParam(required = false) Long fromWalletId,
                                                  @RequestParam(required = false) Long toWalletId,
                                                  @RequestParam(required = false) TransactionStatus status);
}
