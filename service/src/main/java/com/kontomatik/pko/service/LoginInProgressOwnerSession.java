package com.kontomatik.pko.service;

import com.kontomatik.pko.domain.FlowId;
import com.kontomatik.pko.domain.PkoSessionId;
import com.kontomatik.pko.domain.Token;

public record LoginInProgressOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId,
    FlowId flowId,
    Token token
) {
}
