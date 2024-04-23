package com.assignment.ledger.transaction.adapter.web;

import com.assignment.ledger.common.exception.ErrorResult;
import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.dto.StartTransactionResponse;
import com.assignment.ledger.transaction.application.dto.UpdateTransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Transaction Management", description = "manage transaction, support start transaction and update "
        + "transaction. ")
public interface TransactionApi {

    @Operation(summary = "Start transactions, support batch transaction.", description = """
          After transaction started, it is in "processing" status, the actual handling will be conducted asynchronously.
          Client should subscribe relevant message to get update result.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Start transaction successfully."),
            @ApiResponse(responseCode = "500", description = "Internal system error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class)))
    })
    StartTransactionResponse startTransaction(
                                            @Valid @RequestBody List<StartTransactionRequest> startTransactionRequests);

    @Operation(summary = "Request to update transaction.", description = """
            The update process will be conducted asynchronously.
            Client should subscribe relevant message to get update result.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Start transaction successfully."),
            @ApiResponse(responseCode = "500", description = "Internal system error.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class)))
    })
    void updateTransaction(
                    @PathVariable("id") Long id, @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest);
}
