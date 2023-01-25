package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;

public record SuccessfulImport(
  SessionId sessionId,
  Accounts accounts
) implements FinishedImport {
}
