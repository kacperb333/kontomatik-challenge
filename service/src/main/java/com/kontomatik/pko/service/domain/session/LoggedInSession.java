package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;
import com.kontomatik.pko.lib.usecase.login.PkoSessionId;

import java.util.function.Consumer;

public record LoggedInSession(
  OwnerSessionId ownerSessionId,
  OwnerId ownerId,
  PkoSessionId pkoSessionId
) {

  public FinishedSession doWithinSession(Consumer<LoggedInSession> toDo) {
    toDo.accept(this);
    return new FinishedSession(
      ownerSessionId,
      ownerId
    );
  }

  public LoggedInPkoSession asLoggedInPkoSession() {
    return new LoggedInPkoSession(pkoSessionId);
  }
}
