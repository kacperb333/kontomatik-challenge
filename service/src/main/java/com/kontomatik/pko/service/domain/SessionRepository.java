package com.kontomatik.pko.service.domain;

public interface SessionRepository {

  LoginInProgressSession save(LoginInProgressSession loginInProgressSession);

  FinishedSession save(FinishedSession finishedSession);

  LoginInProgressSession getLoginInProgressSession(SessionId sessionId);

  FinishedSession getFinishedSession(SessionId sessionId);
}
