package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.login.LoggedInPkoSession;
import com.kontomatik.lib.pko.domain.login.LoginInProgressPkoSession;

public record LoginInProgressSession(
  SessionId sessionId,
  LoginInProgressPkoSession pkoSession
) {
  public ImportInProgressSession finishLogin(LoggedInPkoSession loggedInPkoSession) {
    return new ImportInProgressSession(
      sessionId,
      loggedInPkoSession
    );
  }
}
