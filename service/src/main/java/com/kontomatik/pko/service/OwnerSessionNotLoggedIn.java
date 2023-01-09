package com.kontomatik.pko.service;

public class OwnerSessionNotLoggedIn extends RuntimeException {
    public OwnerId ownerId;

    public OwnerSessionNotLoggedIn(OwnerId ownerId) {
        super(String.format("Owner's ('%s') session has not been logged in", ownerId.value()));
        this.ownerId = ownerId;
    }
}
