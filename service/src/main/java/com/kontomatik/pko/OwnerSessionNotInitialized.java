package com.kontomatik.pko;

public class OwnerSessionNotInitialized extends RuntimeException {
    public final OwnerSessionId ownerSessionId;

    public OwnerSessionNotInitialized(OwnerSessionId ownerSessionId) {
        super(String.format("Owner session [%s] has not been initialized", ownerSessionId.value()));
        this.ownerSessionId = ownerSessionId;
    }
}
