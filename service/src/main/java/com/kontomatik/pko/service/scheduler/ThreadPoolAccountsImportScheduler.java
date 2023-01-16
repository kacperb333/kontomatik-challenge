package com.kontomatik.pko.service.scheduler;

import com.kontomatik.pko.service.domain.AccountsImportScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
class ThreadPoolAccountsImportScheduler implements AccountsImportScheduler {

  private static final Logger log = LoggerFactory.getLogger(ThreadPoolAccountsImportScheduler.class);
  private final ExecutorService executorService;
  private final long timeoutMinutes;

  public ThreadPoolAccountsImportScheduler(
    @Value("${accounts-import-scheduler.number-of-threads}") int numberOfThreads,
    @Value("${accounts-import-scheduler.timeout-minutes}") long timeoutMinutes
  ) {
    if (numberOfThreads < 2) {
      throw new IllegalArgumentException("Number of threads in ThreadPoolAccountsImportScheduler has to be at least 2 to avoid deadlock");
    }
    this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    this.timeoutMinutes = timeoutMinutes;
  }

  @Override
  public void schedule(Runnable task) {
    executorService.submit(() -> executeWithTimeoutPreservingInterruptedState(task));
  }

  private void executeWithTimeoutPreservingInterruptedState(Runnable task) {
    try {
      executorService.invokeAll(List.of(Executors.callable(task)), timeoutMinutes, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      log.error("Scheduler interrupted while waiting for timeout", e);
      Thread.currentThread().interrupt();
    }
  }
}
