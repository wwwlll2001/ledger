package com.assignment.ledger.transaction.application.service.processor;

import com.assignment.ledger.transaction.adapter.message.TransactionMessageType;
import com.assignment.ledger.transaction.application.dto.StartTransactionRequest;
import com.assignment.ledger.transaction.application.ports.in.TransactionService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;


/**
 * This processor is used to start transaction for those transactions which just requested.
 *
 */
@Component
@Slf4j
public class TransactionStartProcessor implements TransactionPublicEventProcessor {


    private final TransactionService transactionService;

    @Autowired
    public TransactionStartProcessor(@Lazy TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public boolean canProcess(CloudEvent event) {
        return TransactionMessageType.REQUEST_START.toString().equals(event.getType());
    }

    @Override
    public void process(CloudEvent event) {
        String payload = new String((event.getData().toBytes()));
        try {
            List<StartTransactionRequest> startTransactionRequests =
                                                         getObjectMapper().readValue(payload, new TypeReference<>() {});
            transactionService.startTransaction(startTransactionRequests);
        } catch (Exception e) {
            // handle message error, should be monitored and take action if necessary
            log.error(e.getMessage(), e);
        }
    }
}
