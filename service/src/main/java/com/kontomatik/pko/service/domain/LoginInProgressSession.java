package com.kontomatik.pko.service.domain;

import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;
import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession;

public record LoginInProgressSession(
  SessionId sessionId,
  LoginInProgressPkoSession pkoSession
) {
  static LoginInProgressSession from(
    SessionId sessionId,
    LoginInProgressPkoSession loginInProgressPkoSession
  ) {
    return new LoginInProgressSession(
      sessionId,
      loginInProgressPkoSession
    );
  }

  public ImportInProgressSession finishLogin(LoggedInPkoSession loggedInPkoSession) {
    return new ImportInProgressSession(
      sessionId,
      loggedInPkoSession
    );
  }
}
