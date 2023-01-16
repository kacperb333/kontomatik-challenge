package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.LoginInProgressPkoSession;

public record InitialSession(
  OwnerSessionId ownerSessionId,
  OwnerId ownerId
) {
  public LoginInProgressSession initializeLogIn(LoginInProgressPkoSession loginInProgressPkoSession) {
    return new LoginInProgressSession(
      ownerSessionId,
      ownerId,
      loginInProgressPkoSession.pkoSessionId(),
      loginInProgressPkoSession.flowId(),
      loginInProgressPkoSession.token()
    );
  }
}
