package com.kontomatik.pko.service.domain.session;

public class SessionNotInitialized extends RuntimeException {
  public final SessionId sessionId;

  public SessionNotInitialized(SessionId sessionId) {
    super(String.format("Session [%s] has not been initialized", sessionId.value()));
    this.sessionId = sessionId;
  }
}
