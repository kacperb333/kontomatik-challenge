package com.kontomatik.lib.pko.domain.signin;

record PasswordRequiredPkoSession(
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
}
