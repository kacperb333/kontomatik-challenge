package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.AccountsInfo;
import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession;

public record ImportInProgressSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  ImportFinishedSession finishSuccessful(AccountsInfo accountsInfo) {
    return new ImportFinishedSession(sessionId, accountsInfo);
  }

  ImportFailedSession finishFailed() {
    return new ImportFailedSession(sessionId);
  }
}
