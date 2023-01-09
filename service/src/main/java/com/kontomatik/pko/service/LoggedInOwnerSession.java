package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.PkoSessionId;

public record LoggedInOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId
) {
}
