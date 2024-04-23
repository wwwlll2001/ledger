package com.assignment.ledger.transaction.application.service.processor;

import com.assignment.ledger.transaction.adapter.message.TransactionMessageType;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.ports.in.TransactionService;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;


/**
 * This processor is used to handle transaction logic for updating transactions which have been completed.
 *
 */
@Component
@Slf4j
public class TransactionUpdateProcessor implements TransactionEventProcessor {


    private final TransactionService transactionService;

    @Autowired
    public TransactionUpdateProcessor(@Lazy TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public boolean canProcess(CloudEvent event) {
        return TransactionMessageType.UPDATE.toString().equals(event.getType());
    }

    @Override
    public void process(CloudEvent event) {
        String payload = new String((event.getData().toBytes()));
        try {
            TransactionDto transactionDto = getObjectMapper().readValue(payload, TransactionDto.class);
            transactionService.updateTransaction(transactionDto);
        } catch (Exception e) {
            // handle message error, should be monitored and take action if necessary
            log.error(e.getMessage(), e);
        }
    }
}
