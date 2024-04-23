package com.assignment.ledger.wallet.application.service.processor;

import io.cloudevents.CloudEvent;

/**
 * Interface for processing wallet events.
 * Class implementing this interface should provide implementation for handling wallet events
 */
public interface WalletEventProcessor {

    boolean canProcess(CloudEvent event);

    void process(CloudEvent event);
}
