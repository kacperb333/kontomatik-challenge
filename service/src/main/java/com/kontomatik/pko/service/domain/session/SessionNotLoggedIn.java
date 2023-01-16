package com.kontomatik.pko.service.domain.session;

public class SessionNotLoggedIn extends RuntimeException {
  public SessionId sessionId;

  public SessionNotLoggedIn(SessionId sessionId) {
    super(String.format("Session [%s] has not been logged in", sessionId.value()));
    this.sessionId = sessionId;
  }
}
