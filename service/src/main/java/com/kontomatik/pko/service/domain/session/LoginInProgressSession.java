package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.*;

public record LoginInProgressSession(
  SessionId sessionId,
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
  public LoggedInSession finishLogin(LoggedInPkoSession loggedInPkoSession) {
    return new LoggedInSession(
      sessionId,
      loggedInPkoSession.pkoSessionId()
    );
  }

  public LoginInProgressPkoSession asLoginInProgressPkoSession() {
    return new LoginInProgressPkoSession(
      pkoSessionId,
      flowId,
      token
    );
  }
}
