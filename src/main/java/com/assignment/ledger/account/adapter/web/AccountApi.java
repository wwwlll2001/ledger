package com.assignment.ledger.account.adapter.web;


import com.assignment.ledger.account.application.dto.ChangeAccountStatusRequest;
import com.assignment.ledger.common.exception.ErrorResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Account Management", description = "manage account data, currently only support account status "
        + "modification.")
public interface AccountApi {

    @Operation(summary = "Change account to new status", description = """
                                If current account is the same as the new status or active transaction belongs to this 
                                account existing, the request will fail.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change successfully."),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class))),
            @ApiResponse(responseCode = "400", description = """
                    In valid account status or active transaction belongs to this account existing.""",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class))),
            @ApiResponse(responseCode = "500", description = "Internal system error.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResult.class)))
    })
    @PostMapping("/{id}/status")
    public void changeAccountStatus(@PathVariable Long id,
                                    @Valid @RequestBody ChangeAccountStatusRequest changeAccountStatusRequest);
}
