package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession;

public record InitialSession(
  SessionId SessionId
) {
  public LoginInProgressSession initializeLogIn(LoginInProgressPkoSession loginInProgressPkoSession) {
    return new LoginInProgressSession(
      SessionId,
      loginInProgressPkoSession.pkoSessionId(),
      loginInProgressPkoSession.flowId(),
      loginInProgressPkoSession.token()
    );
  }
}
