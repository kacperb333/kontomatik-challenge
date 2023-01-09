package com.kontomatik.pko.service;

public record InitialOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId
) {
}
