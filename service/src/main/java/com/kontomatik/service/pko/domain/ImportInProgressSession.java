package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;

public record ImportInProgressSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  SuccessfulImport finishSuccessful(Accounts accounts) {
    return new SuccessfulImport(sessionId, accounts);
  }

  FailedImport finishFailed() {
    return new FailedImport(sessionId);
  }
}
