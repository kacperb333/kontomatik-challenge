package com.kontomatik.service.pko.domain;

public interface AccountsImportScheduler {
  void schedule(Runnable task);
}
