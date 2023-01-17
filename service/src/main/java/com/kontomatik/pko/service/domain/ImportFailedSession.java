package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

public record ImportFailedSession(
  SessionId sessionId
) implements FinishedSession {
  @Override
  public AccountsInfo accountsInfo() {
    return AccountsInfo.EMPTY;
  }
}
