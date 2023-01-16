package com.kontomatik.pko.service.persistence;

import com.kontomatik.pko.service.domain.session.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemorySessionRepository implements SessionRepository {
  private final ConcurrentMap<SessionId, LoginInProgressSession> inProgressSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, LoggedInSession> loggedInSessions = new ConcurrentHashMap<>();
  private final ConcurrentMap<SessionId, FinishedSession> finishedSessions = new ConcurrentHashMap<>();

  @Override
  public LoginInProgressSession save(LoginInProgressSession loginInProgressSession) {
    var key = loginInProgressSession.sessionId();
    inProgressSessions.put(key, loginInProgressSession);
    return loginInProgressSession;
  }

  @Override
  public LoggedInSession save(LoggedInSession loggedInSession) {
    var key = loggedInSession.sessionId();
    inProgressSessions.remove(key);
    loggedInSessions.put(key, loggedInSession);
    return loggedInSession;
  }

  @Override
  public FinishedSession save(FinishedSession finishedSession) {
    var key = finishedSession.sessionId();
    loggedInSessions.remove(key);
    finishedSessions.put(key, finishedSession);
    return finishedSession;
  }

  @Override
  public LoginInProgressSession getLoginInProgressSession(SessionId sessionId) {
    LoginInProgressSession inProgressSession = inProgressSessions.get(sessionId);
    if (inProgressSession != null) {
      return inProgressSession;
    } else {
      throw new SessionLoginNotInProgress(sessionId);
    }
  }

  @Override
  public LoggedInSession getLoggedInSession(SessionId sessionId) {
    LoggedInSession loggedInSession = loggedInSessions.get(sessionId);
    if (loggedInSession != null) {
      return loggedInSession;
    } else {
      throw new SessionNotLoggedIn(sessionId);
    }
  }
}
