package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;

import java.time.Instant;

public record ImportInProgressSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  FinishedSession finishSuccessful(Instant at, AccountsInfo accountsInfo) {
    return new ImportFinishedSession(sessionId, at, accountsInfo);
  }

  FinishedSession finishFailed(Instant at) {
    return new ImportFailedSession(sessionId, at);
  }
}
