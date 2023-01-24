package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;

public record ImportInProgressSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  ImportFinishedSession finishSuccessful(Accounts accounts) {
    return new ImportFinishedSession(sessionId, accounts);
  }

  ImportFailedSession finishFailed() {
    return new ImportFailedSession(sessionId);
  }
}
