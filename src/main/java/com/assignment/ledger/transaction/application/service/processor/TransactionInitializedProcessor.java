package com.assignment.ledger.transaction.application.service.processor;

import com.assignment.ledger.common.FutureMaker;
import com.assignment.ledger.transaction.adapter.message.TransactionMessageType;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.ports.in.TransactionService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;


/**
 * This processor is used to handle transaction logic for those transactions just started.
 * Support batch transaction handling.
 */
@Component
@Slf4j
public class TransactionInitializedProcessor implements TransactionEventProcessor {


    private final TransactionService transactionService;

    private final FutureMaker futureMaker;

    @Autowired
    public TransactionInitializedProcessor(@Lazy TransactionService transactionService, FutureMaker futureMaker) {
        this.transactionService = transactionService;
        this.futureMaker = futureMaker;
    }

    @Override
    public boolean canProcess(CloudEvent event) {
        return TransactionMessageType.INIT.toString().equals(event.getType());
    }

    @Override
    public void process(CloudEvent event) {
        String payload = new String((event.getData().toBytes()));
        try {
            List<TransactionDto> transactionDtos = getObjectMapper().readValue(payload, new TypeReference<>() {});
            final CompletableFuture[] allResult = transactionDtos.stream()
                    .map(transactionDto -> futureMaker.make(() -> {
                        transactionService.processTransaction(transactionDto);
                        return true;
                    }))
                    .toArray(size -> new CompletableFuture[transactionDtos.size()]);

            CompletableFuture.allOf(allResult).join();
        } catch (Exception e) {
            // handle message error, should be monitored and take action if necessary
            log.error(e.getMessage(), e);
        }
    }
}
