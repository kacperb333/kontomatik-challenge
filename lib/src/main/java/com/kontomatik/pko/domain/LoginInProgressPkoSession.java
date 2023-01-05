package com.kontomatik.pko.domain;

public record LoginInProgressPkoSession(
    SessionId sessionId,
    FlowId flowId,
    Token token
) {
}
