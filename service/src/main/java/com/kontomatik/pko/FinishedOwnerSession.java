package com.kontomatik.pko;

public record FinishedOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId
) {
}
