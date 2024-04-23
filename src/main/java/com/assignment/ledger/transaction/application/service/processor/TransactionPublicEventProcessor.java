package com.assignment.ledger.transaction.application.service.processor;

import io.cloudevents.CloudEvent;

/**
 * Interface for processing public transaction which will interact with client.
 * Class implementing this interface should provide implementation for handling public transaction
 */
public interface TransactionPublicEventProcessor {

    boolean canProcess(CloudEvent event);

    void process(CloudEvent event);
}
