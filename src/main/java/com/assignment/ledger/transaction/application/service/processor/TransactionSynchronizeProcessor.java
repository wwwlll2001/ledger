package com.assignment.ledger.transaction.application.service.processor;

import io.cloudevents.CloudEvent;

/**
 * Interface for synchronize transaction data.
 * Class implementing this interface should provide implementation for synchronizing transaction data to elasticsearch.
 */
public interface TransactionSynchronizeProcessor {

    boolean canProcess(CloudEvent event);

    void process(CloudEvent event);
}
