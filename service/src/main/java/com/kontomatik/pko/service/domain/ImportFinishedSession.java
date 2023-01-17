package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

public record ImportFinishedSession(
  SessionId sessionId,
  AccountsInfo accountsInfo
) implements FinishedSession {
}
