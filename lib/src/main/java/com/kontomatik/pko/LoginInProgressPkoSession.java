package com.kontomatik.pko;

public record LoginInProgressPkoSession(
    PkoSessionId pkoSessionId,
    FlowId flowId,
    Token token
) {
}
