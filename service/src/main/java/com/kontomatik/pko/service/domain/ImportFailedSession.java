package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

import java.time.Instant;

public record ImportFailedSession(
  SessionId sessionId,
  Instant createdAt
) implements FinishedSession {
  @Override
  public AccountsInfo accountsInfo() {
    return AccountsInfo.EMPTY;
  }
}
