package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.accounts.AccountsInfo;
import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;

public record ImportInProgressSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  FinishedSession finishSuccessful(AccountsInfo accountsInfo) {
    return new ImportFinishedSession(sessionId, accountsInfo);
  }

  FinishedSession finishFailed() {
    return new ImportFailedSession(sessionId);
  }
}
