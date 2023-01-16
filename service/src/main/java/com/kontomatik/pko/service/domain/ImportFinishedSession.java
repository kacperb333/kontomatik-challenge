package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;

import java.time.Instant;

public record ImportFinishedSession(
  SessionId sessionId,
  Instant createdAt,
  AccountsInfo accountsInfo
) implements FinishedSession {
}
