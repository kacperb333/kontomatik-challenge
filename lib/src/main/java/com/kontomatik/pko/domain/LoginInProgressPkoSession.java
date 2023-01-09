package com.kontomatik.pko.domain;

public record LoginInProgressPkoSession(
    PkoSessionId pkoSessionId,
    FlowId flowId,
    Token token
) {
}
