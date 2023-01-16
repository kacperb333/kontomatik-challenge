package com.kontomatik.pko.service.scheduler;

import com.kontomatik.pko.service.domain.AccountsImportScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
class ThreadPoolAccountsImportScheduler implements AccountsImportScheduler {

  private final ExecutorService executorService;
  private final long timeoutMinutes;

  public ThreadPoolAccountsImportScheduler(
    @Value("${accounts-import-scheduler.number-of-threads}") int numberOfThreads,
    @Value("${accounts-import-scheduler.timeout-minutes}") long timeoutMinutes
  ) {
    this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    this.timeoutMinutes = timeoutMinutes;
  }

  @Override
  public void schedule(Runnable task) {
    executorService.submit(task);
  }
}
