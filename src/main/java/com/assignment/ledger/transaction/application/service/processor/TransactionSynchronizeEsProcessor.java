package com.assignment.ledger.transaction.application.service.processor;

import com.assignment.ledger.common.FutureMaker;
import com.assignment.ledger.transaction.adapter.message.TransactionMessageType;
import com.assignment.ledger.transaction.application.dto.TransactionDto;
import com.assignment.ledger.transaction.application.service.TransactionSynchronizeService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;


/**
 * This processor is used to synchronize transaction data to elasticsearch.
 *
 */
@Component
@Slf4j
public class TransactionSynchronizeEsProcessor implements TransactionSynchronizeProcessor {

    private final TransactionSynchronizeService transactionSynchronizeService;

    private final FutureMaker futureMaker;

    public TransactionSynchronizeEsProcessor(TransactionSynchronizeService transactionSynchronizeService,
                                             FutureMaker futureMaker) {
        this.transactionSynchronizeService = transactionSynchronizeService;
        this.futureMaker = futureMaker;
    }


    @Override
    public boolean canProcess(CloudEvent event) {
        return TransactionMessageType.INIT.toString().equals(event.getType())
                || TransactionMessageType.COMPLETED.toString().equals(event.getType())
                || TransactionMessageType.UPDATE_FAILED.toString().equals(event.getType())
                || TransactionMessageType.UPDATE_SUCCESS.toString().equals(event.getType());
    }

    @Override
    public void process(CloudEvent event) {
        String payload = new String((event.getData().toBytes()));
        List<TransactionDto> transactionDtos;
        try {
            if (TransactionMessageType.INIT.toString().equals(event.getType())) {
                transactionDtos = getObjectMapper().readValue(payload, new TypeReference<>() {});
            } else {
                transactionDtos = Collections.singletonList(getObjectMapper().readValue(payload, TransactionDto.class));
            }
            final CompletableFuture[] allResult = transactionDtos.stream()
                    .map(transactionDto -> futureMaker.make(() -> {
                        transactionSynchronizeService.synchronizeTransaction(transactionDto);
                        return true;
                    }))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(allResult).join();
        } catch (Exception e) {
            // handle message error, should be monitored and take action if necessary
            log.error(e.getMessage(), e);
        }
    }
}
