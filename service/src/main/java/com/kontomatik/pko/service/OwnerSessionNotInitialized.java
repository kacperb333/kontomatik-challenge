package com.kontomatik.pko.service;

public class OwnerSessionNotInitialized extends RuntimeException {
    public final OwnerId ownerId;

    public OwnerSessionNotInitialized(OwnerId ownerId) {
        super(String.format("Owner's ('%s') session has not been initialized", ownerId.value()));
        this.ownerId = ownerId;
    }
}
