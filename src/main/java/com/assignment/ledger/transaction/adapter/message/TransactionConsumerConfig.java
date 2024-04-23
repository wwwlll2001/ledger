package com.assignment.ledger.transaction.adapter.message;

import com.assignment.ledger.transaction.application.service.processor.TransactionEventProcessor;
import com.assignment.ledger.transaction.application.service.processor.TransactionPublicEventProcessor;
import com.assignment.ledger.transaction.application.service.processor.TransactionSynchronizeProcessor;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring cloud functions for event handling configuration.
 * Each @Bean in this class is a spring cloud function for transaction kafka event handling.
 */
@Configuration
@Slf4j
public class TransactionConsumerConfig {

    private final List<TransactionEventProcessor> transactionEventProcessors;
    private final List<TransactionPublicEventProcessor> transactionPublicEventProcessors;
    private final List<TransactionSynchronizeProcessor> transactionSynchronizeProcessors;

    public TransactionConsumerConfig(List<TransactionEventProcessor> transactionEventProcessors,
                                     List<TransactionPublicEventProcessor> transactionPublicEventProcessors,
                                     List<TransactionSynchronizeProcessor> transactionSynchronizeProcessors) {
        this.transactionEventProcessors = transactionEventProcessors;
        this.transactionPublicEventProcessors = transactionPublicEventProcessors;
        this.transactionSynchronizeProcessors = transactionSynchronizeProcessors;
    }

    /**
     * Consume transaction event and conduct following transaction operations such as start transaction,
     * process transaction and update transaction
     *
     * @return
     */
    @Bean
    public Consumer<CloudEvent> processTransaction() {
        return event -> {
            log.info("receive event:" + event);
            try {
                for (TransactionEventProcessor transactionEventProcessor : transactionEventProcessors) {
                    if (transactionEventProcessor.canProcess(event)) {
                        transactionEventProcessor.process(event);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                //TBD send exception message to failed message handling topic
            }
        };
    }

    /**
     * Consume public transaction event and conduct following transaction operations such as start transaction.
     *
     * @return
     */
    @Bean
    public Consumer<CloudEvent> processPublicTransaction() {
        return event -> {
            log.info("receive event:" + event);
            try {
                for (TransactionPublicEventProcessor transactionPublicEventProcessor :
                                                                                     transactionPublicEventProcessors) {
                    if (transactionPublicEventProcessor.canProcess(event)) {
                        transactionPublicEventProcessor.process(event);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                //TBD send exception message to failed message handling topic
            }
        };
    }

    /**
     * Consume transaction events and synchronize transaction data to elasticsearch for queries.
     *
     * @return
     */
    @Bean
    public Consumer<CloudEvent> synchronizeTransactionData() {
        return event -> {
            log.info("receive event:" + event);
            try {
                for (TransactionSynchronizeProcessor transactionSynchronizeProcessor :
                                                                                     transactionSynchronizeProcessors) {
                    if (transactionSynchronizeProcessor.canProcess(event)) {
                        transactionSynchronizeProcessor.process(event);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                //TBD send exception message to failed message handling topic
            }
        };
    }

}
