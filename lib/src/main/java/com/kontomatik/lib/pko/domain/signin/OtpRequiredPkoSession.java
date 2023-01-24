package com.kontomatik.lib.pko.domain.signin;

public record OtpRequiredPkoSession(
  PkoSessionId pkoSessionId,
  FlowId flowId,
  Token token
) {
}
