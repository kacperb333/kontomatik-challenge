package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;

public sealed interface FinishedImport {

  public record SuccessfulImport(
    ImportId importId,
    Accounts accounts
  ) implements FinishedImport {
  }

  record FailedImport(
    ImportId importId
  ) implements FinishedImport {
  }

  record ImportId(
    String value
  ) {
  }
}
