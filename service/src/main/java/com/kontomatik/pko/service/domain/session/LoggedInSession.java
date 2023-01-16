package com.kontomatik.pko.service.domain.session;

import com.kontomatik.pko.lib.usecase.login.LoggedInPkoSession;
import com.kontomatik.pko.lib.usecase.login.PkoSessionId;

import java.util.function.Consumer;

public record LoggedInSession(
  SessionId sessionId,
  PkoSessionId pkoSessionId
) {

  public FinishedSession doWithinSession(Consumer<LoggedInSession> toDo) {
    toDo.accept(this);
    return new FinishedSession(
      sessionId
    );
  }

  public LoggedInPkoSession asLoggedInPkoSession() {
    return new LoggedInPkoSession(pkoSessionId);
  }
}
