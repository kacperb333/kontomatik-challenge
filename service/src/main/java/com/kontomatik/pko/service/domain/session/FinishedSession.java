package com.kontomatik.pko.service.domain.session;

public record FinishedSession(
  OwnerSessionId ownerSessionId,
  OwnerId ownerId
) {
  public static class SessionLoginNotInProgress extends RuntimeException {
    public final OwnerSessionId ownerSessionId;

    public SessionLoginNotInProgress(OwnerSessionId ownerSessionId) {
      super(String.format("Owner session [%s] has no login in progress", ownerSessionId.value()));
      this.ownerSessionId = ownerSessionId;
    }
  }
}
