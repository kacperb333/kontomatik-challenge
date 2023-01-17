package com.kontomatik.pko.service.domain;

public interface AccountsImportScheduler {
  void schedule(Runnable task);
}
