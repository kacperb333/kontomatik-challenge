package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;

public record ImportFinishedSession(
  SessionId sessionId,
  AccountsInfo accountsInfo
) {
}
