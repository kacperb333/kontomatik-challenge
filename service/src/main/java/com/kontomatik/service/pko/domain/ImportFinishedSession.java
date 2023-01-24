package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;

public record ImportFinishedSession(
  SessionId sessionId,
  Accounts accounts
) {
}
