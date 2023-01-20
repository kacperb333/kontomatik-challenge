package com.kontomatik.service.pko.domain;

public interface SessionRepository {

  LoginInProgressSession save(LoginInProgressSession loginInProgressSession);

  ImportFinishedSession save(ImportFinishedSession finishedSession);

  ImportFailedSession save(ImportFailedSession importFailedSession);

  LoginInProgressSession getLoginInProgressSession(SessionId sessionId);

  AccountsImport getSessionAccountsImport(SessionId sessionId);
}
