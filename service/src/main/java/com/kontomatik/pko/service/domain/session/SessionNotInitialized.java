package com.kontomatik.pko.service.domain.session;

public class SessionNotInitialized extends RuntimeException {
  public final OwnerSessionId ownerSessionId;

  public SessionNotInitialized(OwnerSessionId ownerSessionId) {
    super(String.format("Owner session [%s] has not been initialized", ownerSessionId.value()));
    this.ownerSessionId = ownerSessionId;
  }
}
