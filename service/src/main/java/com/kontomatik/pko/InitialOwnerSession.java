package com.kontomatik.pko;

public record InitialOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId
) {
}
