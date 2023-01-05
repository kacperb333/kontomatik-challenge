package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.SessionId;

public record LoggedInOwnerSession(
    OwnerId ownerId,
    SessionId sessionId
) {
}
