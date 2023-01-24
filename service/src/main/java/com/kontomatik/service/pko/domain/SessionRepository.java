package com.kontomatik.service.pko.domain;

public interface SessionRepository {

  OtpRequiredSession save(OtpRequiredSession otpRequiredSession);

  ImportFinishedSession save(ImportFinishedSession finishedSession);

  ImportFailedSession save(ImportFailedSession importFailedSession);

  OtpRequiredSession getOtpRequiredSession(SessionId sessionId);

  AccountsImport getSessionAccountsImport(SessionId sessionId);
}
