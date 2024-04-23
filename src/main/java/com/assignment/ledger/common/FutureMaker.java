package com.assignment.ledger.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;


/**
 * Future maker to support asynchronous operations if needed.
 */
@Slf4j
@Component
public class FutureMaker {

    private Executor executor;

    public FutureMaker(@Qualifier("applicationTaskExecutor") Executor executor) {
        this.executor = executor;
    }

    public <T> CompletableFuture<T> make(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
