package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.FlowId;
import com.kontomatik.pko.domain.SessionId;
import com.kontomatik.pko.domain.Token;

public record LoginInProgressOwnerSession(
    OwnerId ownerId,
    SessionId sessionId,
    FlowId flowId,
    Token token
) {
}
