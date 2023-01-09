package com.kontomatik.pko.service;

public class OwnerSessionLoginNotInProgress extends RuntimeException {
    public final OwnerId ownerId;

    public OwnerSessionLoginNotInProgress(OwnerId ownerId) {
        super(String.format("Owner's ('%s') session has no login in progress", ownerId.value()));
        this.ownerId = ownerId;
    }
}
