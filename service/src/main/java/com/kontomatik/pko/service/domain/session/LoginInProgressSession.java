package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.*;

public record LoginInProgressSession(
  OwnerSessionId ownerSessionId,
  OwnerId ownerId,
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
  public LoggedInSession finishLogin(LoggedInPkoSession loggedInPkoSession) {
    return new LoggedInSession(
      ownerSessionId,
      ownerId,
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
