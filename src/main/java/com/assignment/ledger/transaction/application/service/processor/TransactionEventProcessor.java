package com.assignment.ledger.transaction.application.service.processor;

import io.cloudevents.CloudEvent;

/**
 * Interface for processing transaction.
 * Class implementing this interface should provide implementation for handling transaction
 */
public interface TransactionEventProcessor {

    boolean canProcess(CloudEvent event);

    void process(CloudEvent event);
}
