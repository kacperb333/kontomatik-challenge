package com.kontomatik.pko.service.domain.session;

public record FinishedSession(
  SessionId sessionId
) {
  public static class SessionLoginNotInProgress extends RuntimeException {
    public final SessionId sessionId;

    public SessionLoginNotInProgress(SessionId sessionId) {
      super(String.format("Session [%s] has no login in progress", sessionId.value()));
      this.sessionId = sessionId;
    }
  }
}
