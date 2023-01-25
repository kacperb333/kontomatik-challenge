package com.kontomatik.service.pko.domain;

public interface SessionRepository {
  OtpRequiredSession save(OtpRequiredSession otpRequiredSession);

  OtpRequiredSession getOtpRequiredSession(SessionId sessionId);
}
