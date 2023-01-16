package com.kontomatik.pko.service.domain;

public class SessionNotFound extends RuntimeException {
  public final SessionId sessionId;

  public SessionNotFound(SessionId sessionId) {
    super(String.format("Session with id [%s] not found", sessionId.value()));
    this.sessionId = sessionId;
  }
}
