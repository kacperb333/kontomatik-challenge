package com.kontomatik.pko;

public record LoggedInOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId
) {
}
