package com.kontomatik.pko.service.domain.accounts;

public interface AccountsImportScheduler {
  void schedule(Runnable task);

  class SchedulerException extends RuntimeException {
    public SchedulerException(Throwable cause) {
      super(cause);
    }
  }
}
