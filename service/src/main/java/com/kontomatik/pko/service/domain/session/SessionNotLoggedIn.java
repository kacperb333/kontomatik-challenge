package com.kontomatik.pko.service.domain.session;

public class SessionNotLoggedIn extends RuntimeException {
  public OwnerSessionId ownerSessionId;

  public SessionNotLoggedIn(OwnerSessionId ownerSessionId) {
    super(String.format("Owner session [%s] has not been logged in", ownerSessionId.value()));
    this.ownerSessionId = ownerSessionId;
  }
}
