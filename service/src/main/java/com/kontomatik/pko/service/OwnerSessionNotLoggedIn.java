package com.kontomatik.pko.service;

public class OwnerSessionNotLoggedIn extends RuntimeException {
    public OwnerSessionId ownerSessionId;

    public OwnerSessionNotLoggedIn(OwnerSessionId ownerSessionId) {
        super(String.format("Owner session [%s] has not been logged in", ownerSessionId.value()));
        this.ownerSessionId = ownerSessionId;
    }
}
