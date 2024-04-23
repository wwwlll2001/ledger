package com.assignment.ledger.wallet.adapter.message;

import com.assignment.ledger.wallet.application.service.processor.WalletEventProcessor;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring cloud functions for event handling configuration.
 * Each @Bean in this class is a spring cloud function for wallet kafka event handling.
 */
@Configuration
@Slf4j
public class WalletConsumerConfig {

    private final List<WalletEventProcessor> walletEventProcessors;

    public WalletConsumerConfig(List<WalletEventProcessor> walletEventProcessors) {
        this.walletEventProcessors = walletEventProcessors;
    }

    /**
     * Consume wallet events and wallet transaction data to elasticsearch for queries.
     *
     * @return
     */
    @Bean
    public Consumer<CloudEvent> synchronizeWalletData() {
        return event -> {
            log.info("receive event:" + event);
            try {
                for (WalletEventProcessor walletEventProcessor : walletEventProcessors) {
                    if (walletEventProcessor.canProcess(event)) {
                        walletEventProcessor.process(event);
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
