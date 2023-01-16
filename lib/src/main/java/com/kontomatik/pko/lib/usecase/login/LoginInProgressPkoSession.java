package com.kontomatik.pko.lib.usecase.login;

public record LoginInProgressPkoSession(
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
}
