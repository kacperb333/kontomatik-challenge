package com.kontomatik.pko;

public class OwnerSessionLoginNotInProgress extends RuntimeException {
    public final OwnerSessionId ownerSessionId;

    public OwnerSessionLoginNotInProgress(OwnerSessionId ownerSessionId) {
        super(String.format("Owner session [%s] has no login in progress", ownerSessionId.value()));
        this.ownerSessionId = ownerSessionId;
    }
}
