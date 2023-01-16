package com.kontomatik.pko.service.api;

import com.kontomatik.pko.service.domain.SessionId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
class SessionHeaderProvider {
  private final String sessionHeaderName;

  public SessionHeaderProvider(@Value("${service.session-header}") String sessionHeaderName) {
    this.sessionHeaderName = sessionHeaderName;
  }

  public HttpHeaders sessionHeader(SessionId sessionId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(sessionHeaderName, sessionId.value());
    return headers;
  }
}
