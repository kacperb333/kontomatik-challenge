package com.kontomatik.pko;

public record LoginInProgressOwnerSession(
    OwnerSessionId ownerSessionId,
    OwnerId ownerId,
    PkoSessionId pkoSessionId,
    FlowId flowId,
    Token token
) {
}
