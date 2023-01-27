package com.kontomatik.service.pko.domain;

import com.kontomatik.lib.pko.domain.signin.LoggedInPkoSession;
import com.kontomatik.lib.pko.domain.signin.OtpRequiredPkoSession;

public record OtpRequiredSession(
  SessionId sessionId,
  OtpRequiredPkoSession pkoSession
) {
  public LoggedInSession finishSignIn(LoggedInPkoSession loggedInPkoSession) {
    return new LoggedInSession(
      sessionId,
      loggedInPkoSession
    );
  }
}
