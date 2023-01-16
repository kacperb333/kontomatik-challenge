package com.kontomatik.pko.service.api;

import com.kontomatik.pko.service.domain.session.OwnerSessionId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
class SessionHeaderProvider {
  private final String ownerSessionHeaderName;

  public SessionHeaderProvider(@Value("${service.owner-session-header}") String ownerSessionHeaderName) {
    this.ownerSessionHeaderName = ownerSessionHeaderName;
  }

  public HttpHeaders ownerSessionHeader(OwnerSessionId ownerSessionId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(ownerSessionHeaderName, ownerSessionId.value());
    return headers;
  }
}
