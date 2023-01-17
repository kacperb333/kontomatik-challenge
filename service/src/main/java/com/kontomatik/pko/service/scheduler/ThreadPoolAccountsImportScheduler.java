package com.kontomatik.pko.service.scheduler;

import com.kontomatik.pko.service.domain.AccountsImportScheduler;
import jakarta.annotation.PreDestroy;
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
  private final ExecutorService backingExecutorService;
  private final long timeoutMinutes;
  private final long shutdownTimeoutSeconds;

  public ThreadPoolAccountsImportScheduler(
    @Value("${accounts-import.scheduler.number-of-threads}") int numberOfThreads,
    @Value("${accounts-import.scheduler.timeout-minutes}") long timeoutMinutes,
    @Value("${accounts-import.scheduler.shutdown-timeout-seconds}") long shutdownTimeoutSeconds
  ) {
    this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    this.backingExecutorService = Executors.newCachedThreadPool();
    this.timeoutMinutes = timeoutMinutes;
    this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
  }

  @Override
  public void schedule(Runnable task) {
    executorService.submit(() -> executeWithTimeoutPreservingInterruptedState(task));
  }

  private void executeWithTimeoutPreservingInterruptedState(Runnable task) {
    try {
      backingExecutorService.invokeAll(List.of(Executors.callable(task)), timeoutMinutes, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      log.error("Scheduler interrupted while waiting for timeout", e);
      Thread.currentThread().interrupt();
    }
  }

  @PreDestroy
  void shutdown() throws InterruptedException {
    log.info("Gracefully shutting down ThreadPoolAccountsImportScheduler");
    executorService.shutdown();
    if (!executorService.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
      log.warn("Timed out while gracefully shutting down ThreadPoolAccountsImportScheduler");
    }
    backingExecutorService.shutdown();
  }
}
