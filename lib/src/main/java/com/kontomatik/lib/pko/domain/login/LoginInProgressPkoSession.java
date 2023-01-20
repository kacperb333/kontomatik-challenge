package com.kontomatik.lib.pko.domain.login;

public record LoginInProgressPkoSession(
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
}
