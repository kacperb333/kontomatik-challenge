package com.kontomatik.pko.service.domain.session;

public interface SessionRepository {

  LoginInProgressSession save(LoginInProgressSession loginInProgressSession);

  LoggedInSession save(LoggedInSession loggedInSession);

  FinishedSession save(FinishedSession finishedSession);

  LoginInProgressSession getLoginInProgressSession(SessionId sessionId);

  LoggedInSession getLoggedInSession(SessionId sessionId);
}
