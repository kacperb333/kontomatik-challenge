package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;

public sealed interface FinishedImport {

  public record SuccessfulImport(
    SessionId sessionId,
    Accounts accounts
  ) implements FinishedImport {
  }

  record FailedImport(
    SessionId sessionId
  ) implements FinishedImport {
  }
}
