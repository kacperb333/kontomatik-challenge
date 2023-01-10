package com.kontomatik.pko;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AccountsImportScheduler {

    private final ExecutorService executorService;

    public AccountsImportScheduler(@Value("${scheduler.number-of-threads}") int numberOfThreads) {
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void submitTask(Runnable task) {
        executorService.submit(task);
    }
}
