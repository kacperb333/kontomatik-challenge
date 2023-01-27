package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.accounts.Accounts;
import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;
import com.kontomatik.service.pko.domain.FinishedImport.FailedImport;
import com.kontomatik.service.pko.domain.FinishedImport.ImportId;
import com.kontomatik.service.pko.domain.FinishedImport.SuccessfulImport;

public record LoggedInSession(
  SessionId sessionId,
  LoggedInPkoSession pkoSession
) {
  SuccessfulImport finishSuccessfully(ImportId importId, Accounts accounts) {
    return new SuccessfulImport(importId, accounts);
  }

  FailedImport finishFailed(ImportId importId) {
    return new FailedImport(importId);
  }
}
